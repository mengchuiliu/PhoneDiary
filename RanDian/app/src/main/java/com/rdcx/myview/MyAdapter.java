package com.rdcx.myview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.rdcx.fragments.DiaryFragment;
import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.fragments.RankFragment;
import com.rdcx.fragments.TaskFragment;
import com.rdcx.fragments.home.AppMapFrament;
import com.rdcx.fragments.home.AppRankFragment;
import com.rdcx.fragments.home.ChatViewFragment;
import com.rdcx.fragments.home.ContactRankFragment;
import com.rdcx.fragments.home.LocationFragment;
import com.rdcx.randian.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/16 0016.
 *
 * @author mengchuiliu
 */
public class MyAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fgs = null;

    public MyAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setFgs(int[] image, int type, long currentTime) {
        this.fgs = getFragments(image, type, currentTime);
    }

    public void setHomeFrgs(int num) {
        this.fgs = getHomeFragment(num);
    }

    @Override
    public Fragment getItem(int position) {
        return fgs.get(position);
    }

    @Override
    public int getCount() {
        return fgs.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    /**
     * 获取主页内维度显示页面
     *
     * @param image       显示图标集合
     * @param type        日期类型
     * @param currentTime 时间
     * @return
     */
    private ArrayList<Fragment> getFragments(int[] image, int type, long currentTime) {
        ArrayList<Fragment> arrayList = new ArrayList<>();
        for (int i = 0; i < (image.length + 2); i++) {
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            bundle.putInt("type", type);
            bundle.putLong("currentTime", currentTime);
            HomeFragment fragment;
            switch ((i == 0 || i == (image.length + 1)) ? 0 : image[i - 1]) {
                case R.mipmap.icon_2:
                    bundle.putString("championType", "6");
                    bundle.putInt("ruleType", 8);
                    fragment = new ContactRankFragment();
                    break;
                case R.mipmap.icon_7:
                    bundle.putString("championType", "2");
                    bundle.putInt("ruleType", 10);
                    fragment = new AppMapFrament();
                    break;
                case R.mipmap.icon_1: // 通话时长
                    bundle.putString("championType", "1");
                    bundle.putInt("ruleType", 3);
                    bundle.putString("title", "通话时长");
                    bundle.putString("defaultText", "[nick]通话时长为[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.icon_5: // 购物时长
                    bundle.putString("championType", "45");
                    bundle.putString("typeNames", "购物");
                    bundle.putInt("ruleType", 4);
                    bundle.putString("title", "购物时长");
                    bundle.putString("defaultText", "[nick]剁手了[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.social: // 社交时长
                    bundle.putString("championType", "43");
                    bundle.putString("typeNames", "社交");
                    bundle.putInt("ruleType", 26);
                    bundle.putString("title", "社交时长");
                    bundle.putString("defaultText", "禀报小主，[nick]玩耍了[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.video:
                    bundle.putString("championType", "61");
                    bundle.putString("typeNames", "影音");
                    bundle.putInt("ruleType", 27);
                    bundle.putString("title", "视频时长");
                    bundle.putString("defaultText", "[nick]陪着小主观看视频[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.game:
                    bundle.putString("championType", "7");
                    bundle.putString("typeNames", "棋牌天地,休闲娱乐,策略塔防,角色动作,飞行射击,速度激情,益智游戏,网络游戏,经营养成,体育竞技,儿童最爱");
                    bundle.putInt("ruleType", 28);
                    bundle.putString("title", "游戏时长");
                    bundle.putString("defaultText", "[nick]玩游戏[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.read:
                    bundle.putString("championType", "8");
                    bundle.putString("typeNames", "阅读,资讯");
                    bundle.putInt("ruleType", 29);
                    bundle.putString("title", "阅读与资讯");
                    bundle.putString("defaultText", "[nick]使用阅读资讯软件[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.money:
                    bundle.putString("championType", "55");
                    bundle.putString("typeNames", "理财");
                    bundle.putInt("ruleType", 30);
                    bundle.putString("title", "理财时长");
                    bundle.putString("defaultText", "[nick]使用理财软件[count]分钟。");
                    fragment = new ChatViewFragment();
                    break;
                case R.mipmap.icon_3:
                    bundle.putString("championType", "3");
                    bundle.putInt("ruleType", 25);
                    fragment = new LocationFragment();
                    break;
                case R.mipmap.icon_8: // 应用使用排行
                    bundle.putString("championType", "4");
                    bundle.putInt("ruleType", 2);
                    bundle.putString("title", "应用使用排行");
                    fragment = new AppRankFragment();
                    break;
                case R.mipmap.time_axis://时光轴
                    bundle.putString("championType", "5");
                    bundle.putInt("ruleType", 23);
                    bundle.putString("title", "时光轴");
                    bundle.putBoolean("timeAixs", true);
                    fragment = new AppRankFragment();
                    break;
                default:
                    fragment = new HomeFragment();
                    break;
            }
            fragment.setArguments(bundle);
            arrayList.add(fragment);
        }
        return arrayList;
    }

    //主页显示页面
    private ArrayList<Fragment> getHomeFragment(int num) {
        ArrayList<Fragment> arrayList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Fragment fragment;
            switch (i) {
                case 0:
                    fragment = new MyHomeFragment();
                    break;
                case 1:
                    fragment = new DiaryFragment();
                    break;
                case 2:
                    fragment = new RankFragment();
                    break;
                case 3:
                    fragment = new TaskFragment();
                    break;
                default:
                    fragment = null;
                    break;
            }
            arrayList.add(fragment);
        }
        return arrayList;
    }
}
