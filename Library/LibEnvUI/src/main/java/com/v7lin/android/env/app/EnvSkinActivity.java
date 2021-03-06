package com.v7lin.android.env.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import com.v7lin.android.env.EnvResBridge;
import com.v7lin.android.env.EnvResManager;
import com.v7lin.android.env.EnvSkinResourcesWrapper;
import com.v7lin.android.env.SystemResMap;
import com.v7lin.android.env.EnvCallback;
import com.v7lin.android.env.widget.EnvUIChanger;
import com.v7lin.android.env.widget.EnvActivityChanger;
import com.v7lin.android.env.widget.EnvViewMap;
import com.v7lin.android.env.widget.EnvViewParams;
import com.v7lin.android.env.widget.XActivityCall;
import com.v7lin.android.env.widget.XViewCall;

import java.lang.reflect.Field;

/**
 * @author v7lin E-mail:v7lin@qq.com
 */
public class EnvSkinActivity extends Activity implements XActivityCall {

	private EnvResBridge mEnvResBridge;
	private EnvUIChanger<Activity, XActivityCall> mEnvUIChanger;

	@Override
	protected void attachBaseContext(Context newBase) {
		// 反射替换 Resources
		ensureEnvRes(newBase);
		super.attachBaseContext(newBase);
	}

	private void ensureEnvRes(Context newBase) {
		if (mEnvResBridge == null) {
			mEnvResBridge = new EnvResBridge(newBase, newBase.getResources(), EnvResManager.getGlobal());

			try {
				Resources skinResourcesWrapper = new EnvSkinResourcesWrapper(newBase.getResources(), mEnvResBridge);
				Field mResourcesField = newBase.getClass().getDeclaredField("mResources");
				mResourcesField.setAccessible(true);
				mResourcesField.set(newBase, skinResourcesWrapper);
			} catch (NoSuchFieldException e) {
				//e.printStackTrace();
			} catch (IllegalAccessException e) {
				//e.printStackTrace();
			}
		}
	}

	public EnvResBridge getEnvResBridge() {
		return mEnvResBridge;
	}

	public void setSystemResMap(SystemResMap systemResMap) {
		if (mEnvResBridge != null) {
			mEnvResBridge.setSystemResMap(systemResMap);
		}
	}

	@Override
	public View onCreateView(final String name, final Context context, final AttributeSet attrs) {
		View view = null;

		// 转换视图
		String viewName = name;
		if (TextUtils.equals(viewName, "view")) {
			viewName = attrs.getAttributeValue(null, "class");
		}
		String transferName = EnvViewMap.getInstance().transfer(viewName, true);
		if (!TextUtils.isEmpty(transferName) && transferName.indexOf(".") > -1) {
			LayoutInflater inflater = LayoutInflater.from(this);
			try {
				view = inflater.createView(transferName, null, attrs);
			} catch (InflateException e) {
				// In this case we want to let the base class take a crack at it.
			} catch (ClassNotFoundException e) {
				// In this case we want to let the base class take a crack at it.
			} catch (Exception e) {
				// In this case we want to let the base class take a crack at it.
			}
		}
		if (view != null && view instanceof EnvCallback) {
			EnvCallback callback = (EnvCallback) view;
			EnvViewParams params = EnvViewMap.getInstance().obtainViewParams(view.getClass());
			if (params != null) {
				EnvUIChanger<View, XViewCall> changer = callback.ensureEnvUIChanger(mEnvResBridge, params.allowSysRes);
				changer.applyStyle(attrs, params.defStyleAttr, params.defStyleRes, view.isInEditMode());
			}
		}

		if (view == null) {
			view = super.onCreateView(name, context, attrs);
		}

		return view;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEnvUIChanger = new EnvActivityChanger<Activity, XActivityCall>(this, mEnvResBridge, false);
		mEnvUIChanger.applyStyle(null, 0, 0, false);
		mEnvUIChanger.scheduleSkin(this, this, false);
		mEnvUIChanger.scheduleFont(this, this, false);
	}

	/**
	 * 可继承 {@link EnvSkinActivity} 重写该函数
	 * 
	 * 设置视图资源，实现不支持换肤功能的视图，进行换肤
	 */
	public void scheduleSkin() {
		if (mEnvUIChanger != null) {
			mEnvUIChanger.scheduleSkin(this, this, false);
		}
	}

	/**
	 * 可继承 {@link EnvSkinActivity} 重写该函数
	 * 
	 * 设置视图字体，实现不支持换字体功能的视图，进行换字体
	 */
	public void scheduleFont() {
		if (mEnvUIChanger != null) {
			mEnvUIChanger.scheduleFont(this, this, false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		scheduleSkin();
		scheduleFont();
	}

	@Override
	public void scheduleBackgroundDrawable(Drawable background) {
		getWindow().setBackgroundDrawable(background);
	}

	public void setWindowBackgroundResource(int resid) {
		getWindow().setBackgroundDrawable(getEnvResBridge().getDrawable(resid, getTheme()));
		if (mEnvUIChanger != null) {
			mEnvUIChanger.applyAttr(this, this, android.R.attr.windowBackground, resid, false);
		}
	}
}
