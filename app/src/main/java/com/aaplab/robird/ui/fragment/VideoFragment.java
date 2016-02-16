package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.aaplab.robird.R;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.MediaPlayerWrapper;
import com.volokh.danylo.video_player_manager.ui.ScalableTextureView;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 14.02.16.
 */
public class VideoFragment extends BaseFragment implements PlayerItemChangeListener,
        MediaPlayerWrapper.MainThreadMediaPlayerListener, View.OnClickListener {

    public static VideoFragment create(String video) {
        final VideoFragment fragment = new VideoFragment();

        Bundle args = new Bundle();
        args.putString("video", video);
        fragment.setArguments(args);

        return fragment;
    }

    @Bind(R.id.progress)
    ProgressBar mProgressBar;

    @Bind(R.id.video_player)
    VideoPlayerView mVideoPlayerView;

    @Bind(R.id.play_button)
    ImageView mPlayButton;

    private VideoPlayerManager mVideoPlayerManager;
    private String mVideo;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVideo = getArguments().getString("video");
        mVideoPlayerView.setScaleType(ScalableTextureView.ScaleType.FILL);
        mVideoPlayerManager = new SingleVideoPlayerManager(this);
        mVideoPlayerView.addMediaPlayerListener(this);
        mPlayButton.setOnClickListener(this);
        mVideoPlayerManager.playNewVideo(null, mVideoPlayerView, mVideo);
    }

    @Override
    public void onStop() {
        super.onStop();
        mVideoPlayerManager.stopAnyPlayback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVideoPlayerManager.resetMediaPlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onClick(View v) {
        mVideoPlayerManager.playNewVideo(null, mVideoPlayerView, mVideo);
    }

    @Override
    public void onPlayerItemChanged(MetaData currentItemMetaData) {

    }

    @Override
    public void onVideoSizeChangedMainThread(int width, int height) {

    }

    @Override
    public void onVideoPreparedMainThread() {
        mProgressBar.setVisibility(View.GONE);
        mPlayButton.setVisibility(View.GONE);
    }

    @Override
    public void onVideoCompletionMainThread() {
        mPlayButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onErrorMainThread(int what, int extra) {

    }

    @Override
    public void onBufferingUpdateMainThread(int percent) {

    }

    @Override
    public void onVideoStoppedMainThread() {
        mPlayButton.setVisibility(View.VISIBLE);
    }
}
