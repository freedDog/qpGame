package com.game.framework.jdbc.executer;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.game.framework.jdbc.executer.param.JdbcParam;
import com.game.framework.jdbc.utils.JdbcUtils;



/**
 * Jdbc更新器
 * JdbcUpdate.java
 * @author JiangBangMing
 * 2019年1月3日下午5:03:39
 */
public abstract class JdbcUpdate<T> extends JdbcExecuter<T> {
	protected boolean generatedKeys; // 返回主键
	protected boolean totalResult; // 是否汇总结果, 更新返回int代表成功数量

	public JdbcUpdate(String sql, Class<?>[] parameterTypes, Type retType, boolean generatedKeys, boolean totalResult) throws Exception {
		super(sql, parameterTypes, retType);
		this.generatedKeys = generatedKeys;
		this.totalResult = totalResult;
	}

	@Override
	public Process process(Object... args) throws Exception {
		return process(sql, params, args);
	}

	@Override
	protected PreparedStatement createPreparedStatement(Connection conn, JdbcExecuter.Process process) throws Exception {
		final Process process0 = (Process) process;
		return createPreparedStatement(conn, this.sql, process0.batchArgs, this.generatedKeys);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<T> doInPreparedStatement(PreparedStatement ps, JdbcExecuter.Process process) throws Exception {
		return doInPreparedStatement(ps, (Class<T>) getReturnType(), generatedKeys, totalResult);
	}

	/** 解析参数生成过程 **/
	public static Process process(String sql, List<JdbcParam> params, Object... args) throws Exception {
		// 遍历获取
		Process process = new Process();
		if (!process(sql, params, process, args)) {
			return null; // 失败
		}
		return process;
	}

	/** 解析参数生成过程 **/
	public static boolean process(String sql, List<JdbcParam> params, Process process, Object... args) throws Exception {
		// 遍历获取
		int paramSize = (params != null) ? params.size() : 0;
		if (paramSize <= 0) {
			// 无参数处理,空过程.
			process.batchArgs.add(new Object[0]); // 增加一个过程
			return true;
		}

		// 如果是更新, 检测是否是数组方式提交.
		Object fristArg = args[0];
		boolean isLoop = false; // 判断第一个参数是否是数组
		int loopCount = 1; // 单独保留, 如果是对象本身也是1
		if (Collection.class.isInstance(fristArg)) {
			// 这里只支持list,如果是集合, 得转成list
			if (!List.class.isInstance(fristArg)) {
				fristArg = new ArrayList<>((Collection<?>) fristArg);
				args[0] = fristArg;
			}

			// 遍历处理数据
			loopCount = ((Collection<?>) fristArg).size();
			isLoop = true;
		} else if (Arrays.class.isInstance(fristArg)) {
			loopCount = ((Object[]) fristArg).length;
			isLoop = true;
		}

		// 遍历获取
		for (int index = 0; index < loopCount; index++) {
			// 处理单列数据
			Object[] oneArgs = new Object[paramSize];
			for (int i = 0; i < paramSize; i++) {
				JdbcParam param = params.get(i);
				Object paramObj = param.getParam(args, (isLoop) ? index : -1);
				oneArgs[i] = JdbcUtils.toSqlObject(paramObj);
			}
			process.batchArgs.add(oneArgs);
			// System.out.println(index + " " + Arrays.toString(oneArgs));
		}
		return true;
	}

	/** 创建PreparedStatement **/
	public static PreparedStatement createPreparedStatement(Connection conn, String sql, List<Object[]> batchArgs, boolean generatedKeys) throws Exception {
		// 创建PreparedStatement
		int autoGeneratedKeys = (generatedKeys) ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
		PreparedStatement ps = conn.prepareStatement(sql, autoGeneratedKeys);
		// 获取参数, 遍历写入
		int argsCount = (batchArgs != null) ? batchArgs.size() : 0;
		for (int i = 0; i < argsCount; i++) {
			// 遍历写入单个数据
			Object[] args = batchArgs.get(i);
			for (int j = 0; j < args.length; j++) {
				Object arg = args[j];
				if (arg == null) {
					ps.setObject(j + 1, null, java.sql.Types.NULL); // 空处理
					continue;
				}
				ps.setObject(j + 1, arg, JdbcUtils.getSqlType(arg.getClass()));
			}
			ps.addBatch();
		}
		return ps;
	}

	/** 处理结果回馈 **/
	@SuppressWarnings("unchecked")
	public static <T> List<T> doInPreparedStatement(PreparedStatement ps, Class<T> retType, boolean generatedKeys, boolean totalResult) throws Exception {
		// 提交執行
		int[] rets = ps.executeBatch();

		// 检测有没有返回类型
		if (isEmptyReturn(retType)) {
			return null;
		}

		// 返回值处理
		if (generatedKeys) {
			// 经过研究, 只有插入(insert)和覆盖(replace)才能返回主键, 并且要启用只增Id才能返回.
			ResultSet resultSet = null;
			try {
				resultSet = ps.getGeneratedKeys();
				// 遍历处理
				List<Object> list = new ArrayList<>();
				while (resultSet != null && resultSet.next()) {
					// 反射解析对象
					Object obj = resultSet.getObject(1);
					obj = JdbcUtils.toJavaObject(obj);
					list.add(obj);
				}
				return (List<T>) list;
			} finally {
				close(resultSet);
			}
		} else if (totalResult) {
			// 汇总数据
			int totalCount = 0;
			int count0 = (rets != null) ? rets.length : 0;
			for (int i = 0; i < count0; i++) {
				totalCount += rets[i];
			}

			// 单个返回
			List<Object> retList = new ArrayList<>();
			retList.add(JdbcUtils.toJavaObject(totalCount, retType));
			return (List<T>) retList;
		}

		// 分散返回
		List<Object> retList = new ArrayList<>();
		int rsize = (rets != null) ? rets.length : 0;
		for (int i = 0; i < rsize; i++) {
			retList.add(JdbcUtils.toJavaObject(rets[i], retType));
		}
		return (List<T>) retList;
	}

	/** 处理过程 **/
	public static class Process implements JdbcExecuter.Process {
		public final List<Object[]> batchArgs = new ArrayList<>();

		/** 输出列表数组字符串 **/
		@Override
		public String toString() {
			int count = (batchArgs != null) ? batchArgs.size() : 0;
			if (count <= 0) {
				return "[]";
			}

			// 遍历输出文本
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("[");
			for (int i = 0; i < count; i++) {
				Object[] args = batchArgs.get(i);
				strBuf.append(Arrays.toString(args));
				// 不是最后一个加上,
				if (i < count - 1) {
					strBuf.append(",");
				}
			}
			strBuf.append("]");
			return strBuf.toString();
		}
	}

}
