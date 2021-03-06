package com.v7lin.android.env.impl;

import android.content.Context;

import com.v7lin.android.env.EnvSetup;

/**
 * @author v7lin E-mail:v7lin@qq.com
 */
public class NullEnvSetup implements EnvSetup {

	private NullEnvSetup() {
		super();
	}

	@Override
	public String getSkinPath(Context context) {
		return "";
	}

	@Override
	public String getFontPath(Context context) {
		return "";
	}
	
	private static class NullEnvSetupHolder {
		private static final NullEnvSetup INSTANCE = new NullEnvSetup();
	}

	public static NullEnvSetup getInstance() {
		return NullEnvSetupHolder.INSTANCE;
	}
}
