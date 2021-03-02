package com.cucund.spring.lock;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SPelUtil {

	private static final ExpressionParser parser = new SpelExpressionParser();

	private static final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * 解析 spel 表达式
	 *
	 * @param method    方法
	 * @param arguments 参数
	 * @param sPel      表达式
	 * @param clazz     返回结果的类型
	 * @param defaultResult 默认结果
	 * @return 执行sPel表达式后的结果
	 */
	public static <T> T parseSPel(Method method, Object[] arguments, String sPel, Class<T> clazz, T defaultResult) {
		String[] params = discoverer.getParameterNames(method);
		EvaluationContext context = new StandardEvaluationContext();
		for (int len = 0; len < params.length; len++) {
			context.setVariable(params[len], arguments[len]);
		}
		try {
			Expression expression = parser.parseExpression(sPel);
			return expression.getValue(context, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
