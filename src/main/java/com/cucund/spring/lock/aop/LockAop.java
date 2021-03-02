package com.cucund.spring.lock.aop;

import com.cucund.spring.lock.LockEntity;
import com.cucund.spring.lock.LockType;
import com.cucund.spring.lock.SPelUtil;
import com.cucund.spring.lock.anno.Lock;
import com.cucund.spring.lock.manager.LockManager;
import com.cucund.spring.lock.manager.NativeLockManager;
import com.cucund.spring.lock.manager.RedisLockManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LockAop implements ApplicationContextAware {

	public static final String POINT_CUT = "pointcut()";


	@Pointcut("@annotation(com.cucund.spring.lock.anno.Lock)")
	public void pointcut() {log.info("锁 切面 注入---------> ");}

	Map<LockType, LockManager> map = new HashMap<>();

	/**
	 * 前置通知
	 */
	@Before(POINT_CUT)
	public void before(JoinPoint point) throws InterruptedException {
		LockEntity lockEntity = getLockEntity(point);
		if (lockEntity == null) return;
		LockManager lockManager = map.get(lockEntity.getLockType());
		log.info("INFO:WAIT:{},time:{}" , lockEntity.getKey(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()).getTime());
		lockManager.tryLock(lockEntity);
		log.info("INFO:START:{},time:{}" , lockEntity.getKey(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()).getTime());
	}

	/**
	 * 后置通知
	 */
	@After(POINT_CUT)
	public void after(JoinPoint point){
		LockEntity lockEntity = getLockEntity(point);
		if (lockEntity == null) return;
		LockManager lockManager = map.get(lockEntity.getLockType());
		lockManager.unLock(lockEntity);
		log.info("INFO:END:{},time:{}" , lockEntity.getKey(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()).getTime());
	}

	private LockEntity getLockEntity(JoinPoint point) {
		Method method = getMethod(point);
		Lock lock = method.getAnnotation(Lock.class);
		String key = SPelUtil.parseSPel(method, point.getArgs(), lock.value(), String.class, "");
		if (key == null)
			return null;
		LockEntity lockEntity = LockEntity.builder()
				.lockType(lock.lockType())
				.key(key)
				.time(lock.time())
				.timeUnit(lock.unit())
				.build();
		return lockEntity;
	}

	private Method getMethod(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		if (method.getDeclaringClass().isInterface()) {
			try {
				method = joinPoint
						.getTarget()
						.getClass()
						.getDeclaredMethod(joinPoint.getSignature().getName(),
								method.getParameterTypes());
			} catch (SecurityException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return method;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		map.put( LockType.NATIVE , applicationContext.getBean(NativeLockManager.class));
		map.put( LockType.REDIS  , applicationContext.getBean(RedisLockManager.class));
	}
}
