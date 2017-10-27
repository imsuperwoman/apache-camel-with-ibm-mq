package com.awf.spring.helper;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

@Component
public class DebugHelper extends AbstractHelper {

	//	@Autowired
	//	private AppConfig appConfig;

	@Value("${debug.mode:false}")
	private boolean isDebugMode;

	public void debug(Logger logger, Collection<?> collection) {
		if (this.isDebugMode()) {
			for (Object obj : collection) {
				debug(logger, obj);
			}
		}
	}

	public void debug(Logger logger, Object obj) {
		if (this.isDebugMode()) {
			String reflection = ToStringBuilder.reflectionToString(obj);
			logger.debug(reflection);
		}
	}

	public long getElapsedMilliseconds(Date start, Date end) {
		Preconditions.checkNotNull(start);
		Preconditions.checkNotNull(end);

		long startTime = start.getTime();
		long endTime = end.getTime();
		return this.getElapsedMilliseconds(startTime, endTime);
	}

	public long getElapsedMilliseconds(long startTime, long endTime) {
		return endTime - startTime;
	}

	public Period getElapsedTime(Date start, Date end) {
		Preconditions.checkNotNull(start);
		Preconditions.checkNotNull(end);

		long startTime = start.getTime();
		long endTime = end.getTime();
		return this.getElapsedTime(startTime, endTime);
	}

	/**
	 * this method is very slow and dont use in production
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Period getElapsedTime(long startTime, long endTime) {
		Interval interval = new Interval(startTime, endTime);
		return interval.toPeriod();
	}

	public boolean isDebugMode() {
		return isDebugMode;
	}

}
