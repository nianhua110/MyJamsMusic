package io.github.nianhua110.myjamsmusic.ListViewFragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;

import java.util.HashMap;

import io.github.nianhua110.myjamsmusic.MainActivity;
import io.github.nianhua110.myjamsmusic.R;
import io.github.nianhua110.myjamsmusic.Utils.Common;

/**
 * Created by kankan on 2016/5/23.
 */
public class ListViewFragment extends Fragment {

    private Context mContext;
    private ListViewFragment mFragment;
    private Common mApp;
    private View mRootView;
    private QuickScroll mQuickScroll;
    //private ListViewCardsAdapter mListViewAdapter;
    private HashMap<Integer, String> mDBColumnsMap;
    private ListView mListView;
    private TextView mEmptyTextView;

    private RelativeLayout mSearchLayout;
    private EditText mSearchEditText;
    private int mFragmentId;
    private ArrayAdapter tArrayAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (View) inflater.inflate(R.layout.fragment_list_view, container, false);
        //mRootView.setBackgroundColor(Color.RED);

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mFragment = this;

        //Grab the fragment. This will determine which data to load into the cursor.
     //   mFragmentId = getArguments().getInt(Common.FRAGMENT_ID);
      //  mFragmentTitle = getArguments().getString(MainActivity.FRAGMENT_HEADER);
        mDBColumnsMap = new HashMap<Integer, String>();

        //Init the search fields.
        mSearchLayout = (RelativeLayout) mRootView.findViewById(R.id.search_layout);
        mSearchEditText = (EditText) mRootView.findViewById(R.id.search_field);

      //  mSearchEditText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        mSearchEditText.setPaintFlags(mSearchEditText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
      //  mSearchEditText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
        mSearchEditText.setTextColor(Color.BLUE);
        mSearchEditText.setFocusable(true);
        mSearchEditText.setCursorVisible(true);

        mQuickScroll = (QuickScroll) mRootView.findViewById(R.id.quickscroll);

        mListView = (ListView) mRootView.findViewById(R.id.generalListView);
        mListView.setVerticalScrollBarEnabled(false);
        tArrayAdapter = new ArrayAdapter(mContext, R.layout.test_item_layout, R.id.textViewItem);
        /////// TODO: 2016/5/24  下面测试代码
        mListView.setDivider(mContext.getResources().getDrawable(R.drawable.icon_list_divider));
        mListView.setDividerHeight(1);
        tArrayAdapter.add("dfd");
        mListView.setAdapter(tArrayAdapter);
        tArrayAdapter.add("dfd");
        tArrayAdapter.add("dfd");
        tArrayAdapter.notifyDataSetChanged();
        mEmptyTextView = (TextView) mRootView.findViewById(R.id.empty_view_text);
        mEmptyTextView.setText("test");
        mEmptyTextView.setTextColor(Color.BLACK);
        return mRootView;
    }
}
