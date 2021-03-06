package com.game.framework.framework.timer.action;

import com.game.framework.component.action.ActionQueue;
import com.game.framework.framework.mgr.ServiceMgr;
import com.game.framework.framework.timer.HourTimer;

/**
 * 每小时定时器<br>
 * 增加执行队列, 防止阻塞定时器线程.
 * ActionHourTimer.java
 * @author JiangBangMing
 * 2019年1月3日下午4:20:41
 */
public abstract class ActionHourTimer extends HourTimer
{
	protected final ActionQueue queue; // 执行队列

	public ActionHourTimer(String name)
	{
		super(name);
		queue = new ActionQueue(ServiceMgr.getExecutor());
	}

	@Override
	public void run(final long prevTime, final long nowTime, final int count)
	{
		queue.enqueue(new Runnable()
		{
			@Override
			public void run()
			{
				execute(prevTime, nowTime, runCount++);
			}
		});
	}
}