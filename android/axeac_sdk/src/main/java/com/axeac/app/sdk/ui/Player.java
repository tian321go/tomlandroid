package com.axeac.app.sdk.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.axeac.app.sdk.R;
import com.axeac.app.sdk.ui.base.Component;
import com.axeac.app.sdk.utils.StaticObject;

/**
 * describe:Player component
 * 播放组件
 * @author axeac
 * @version 1.0.0
 */
public class Player extends Component {

	/**
	 * VideoView对象
	 * */
	private VideoView mVideoView;

	/**
	 * 标题文本
	 * */
	private String title;
	/**
	 * 标题图标
	 * */
	private String icon;
	/**
	 * 要打开的文件地址
	 * */
	private String url;
	/**
	 * 文件类型  Video\Sound
	 * 默认值为video
	 * */
	private String option = "video";
	
	public Player(Activity ctx) {
		super(ctx);
		mVideoView = new VideoView(ctx);
		mVideoView.setLayoutParams(new ViewGroup.LayoutParams(
				StaticObject.deviceWidth, StaticObject.deviceHeight));
	}

	/**
	 * 设置标题文本
	 * @param title
	 * 标题文本
	 * */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 设置标题图标
	 * @param icon
	 * 标题图标
	 * */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * 设置要播放的文件的地址
	 * @param url
	 * 要播放的文件的地址
	 * */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 设置要播放的文件类型
	 * @param option
	 * 可选值 Video\Sound
	 * */
	public void setOption(String option) {
		this.option = option;
	}

	/**
	 * 执行方法
	 * */
	@Override
	public void execute() {
		if (this.width == "-1" && this.height == -1) {
			mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
		} else {
			if (this.width == "-1") {
				mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, this.height, 1));
			} else if (this.height == -1) {
				if (this.width.endsWith("%")) {
					int viewWeight = 100 - (int) Float.parseFloat(this.width.substring(0, this.width.indexOf("%")));
					mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, viewWeight));
				} else {
					mVideoView.setLayoutParams(new LinearLayout.LayoutParams(Integer.parseInt(this.width), LinearLayout.LayoutParams.WRAP_CONTENT));
				}
			} else {
				if (this.width.endsWith("%")) {
					int viewWeight = 100 - (int) Float.parseFloat(this.width.substring(0, this.width.indexOf("%")));
					mVideoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, viewWeight));
				} else {
					mVideoView.setLayoutParams(new LinearLayout.LayoutParams(Integer.parseInt(this.width), this.height));
				}
			}
		}
		option = "video";
		if (url != null) {
			if (option.equals("sound")) {
				
			} else {
				mVideoView.setMediaController(new MediaController(ctx));
				mVideoView.setVideoPath(url);
				mVideoView.requestFocus();
				mVideoView.seekTo(0);
				mVideoView.start();
			}
		} else {
			Toast.makeText(ctx, R.string.axeac_toast_exp_tovideopath, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public String getValue() {
		return null;
	}

	/**
	 * 返回当前视图
	 * */
	@Override
	public View getView() {
		return mVideoView;
	}

	@Override
	public void repaint() {
		
	}

	@Override
	public void starting() {
		
	}

	@Override
	public void end() {
		
	}
}