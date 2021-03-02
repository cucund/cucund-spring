package com.cucund.spring.lock;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
@Builder
public class LockEntity {

	private String key;

	private LockType lockType;

	private TimeUnit timeUnit;

	private Integer time;

}
