package io.github.nianhua110.myjamsmusic.Drawers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.sql.RowSet;

import io.github.nianhua110.myjamsmusic.R;

/**
 * Created by kankan on 2016/5/23.
 */
public class NavigationDrawerFragment  extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.navigation_drawer_layout, null);
      //  return ;
        return rootView;
    }
}
