package cn.bingoogolapple.media.ui.fragment;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;
import cn.bingoogolapple.media.R;
import cn.bingoogolapple.media.engine.MediaScanner;
import cn.bingoogolapple.media.model.MediaFile;
import cn.bingoogolapple.media.ui.widget.Divider;
import cn.bingoogolapple.media.util.ThreadUtil;
import cn.bingoogolapple.media.util.StringUtil;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/9/7 下午5:18
 * 描述:
 */
public class AudioFragment extends BaseFragment implements BGAOnRVItemClickListener {
    private RecyclerView mDataRv;
    private MusicAdapter mMusicAdapter;
    private AudioContentObserver mAudioContentObserver;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_audio);
        mDataRv = getViewById(R.id.rv_audio_data);
    }

    @Override
    protected void setListener() {
        mMusicAdapter = new MusicAdapter(mDataRv);
        mMusicAdapter.setOnRVItemClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        registerAudioContentObserver();
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDataRv.setLayoutManager(layoutManager);
        mDataRv.addItemDecoration(new Divider(mActivity));
        mDataRv.setAdapter(mMusicAdapter);

        reloadData();
    }

    private void reloadData() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                final List<MediaFile> mediaFiles = MediaScanner.scanAudio();
                ThreadUtil.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mMusicAdapter.setDatas(mediaFiles);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterAudioContentObserver();
        super.onDestroy();
    }

    private void registerAudioContentObserver() {
        mAudioContentObserver = new AudioContentObserver(new Handler());
        mActivity.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mAudioContentObserver);
    }

    private void unregisterAudioContentObserver() {
        mActivity.getContentResolver().unregisterContentObserver(mAudioContentObserver);
    }

    @Override
    public void onRVItemClick(ViewGroup viewGroup, View view, int position) {

    }

    private final class MusicAdapter extends BGARecyclerViewAdapter<MediaFile> {

        public MusicAdapter(RecyclerView recyclerView) {
            super(recyclerView, R.layout.item_audio);
        }

        @Override
        protected void fillData(BGAViewHolderHelper helper, int position, MediaFile model) {
            helper.setText(R.id.tv_item_audio_name, model.name);
            helper.setText(R.id.tv_item_audio_size, Formatter.formatFileSize(mContext, model.size));
            helper.setText(R.id.tv_item_audio_duration, StringUtil.formatTime(model.duration));
        }
    }

    private final class AudioContentObserver extends ContentObserver {

        public AudioContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            reloadData();
        }
    }
}