package com.fat246.cybercar.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fat246.cybercar.R;
import com.fat246.cybercar.activities.QRCode.QRCodeActivity;
import com.fat246.cybercar.application.MyApplication;
import com.fat246.cybercar.beans.Order;
import com.fat246.cybercar.tools.HelpInitBoomButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class MyOrdersActivity extends AppCompatActivity implements BoomMenuButton.OnSubButtonClickListener {


    private PtrClassicFrameLayout mPtrFrame;
    private ListView mListView;

    private List<Order> mDataList = new ArrayList<>();

    //Adapter
    private OrderAdapter mAdapter;

    //BoomData
    //BoomData
    private String[] title = new String[]{
            "详细信息",
            "二维码",
            "删除"
    };
    private int[][] colors;

    //Resource
    private static int[] drawablesResource = new int[]{
            R.drawable.ic_info,
            R.drawable.ic_qrcode,
            R.drawable.ic_delete
    };
    private Drawable[] drawables;
    public static Integer posi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initToolbar();

        initBoom();

        findView();

        initList();

        initPtr();

        beginToRefresh();

        setListener();
    }

    private void initBoom() {

        colors = new int[3][2];

        //随机数
        Random mRandom = new Random();

        for (int i = 0; i < 3; i++) {

            int r = Math.abs(mRandom.nextInt() % HelpInitBoomButton.Colors.length);

            colors[i][1] = Color.parseColor(HelpInitBoomButton.Colors[r]);

            colors[i][0] = Util.getInstance().getPressedColor(colors[i][1]);
        }

        //加载图片资源
        drawables = new Drawable[3];

        for (int i = 0; i < 3; i++) {

            drawables[i] = ContextCompat.getDrawable(this, drawablesResource[i]);
        }
    }

    //setListener
    private void setListener() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                toQRCode(position);
            }
        });
    }

    //toQRCode
    private void toQRCode(int position) {

        Log.e("position>>>", position + "");

        //倒转
        position = mDataList.size() - position - 1;

        Order order = mDataList.get(position);

        Intent mIntent = new Intent(MyOrdersActivity.this, QRCodeActivity.class);

        Bundle mBundle = new Bundle();

        mBundle.putInt(QRCodeActivity.Action, 0);
        mBundle.putString("Order_ID", order.getOrder_ID());
        mBundle.putString("User_Tel", order.getUser_Tel());
        mBundle.putDouble("Order_GasPrice", order.getOrder_GasPrice());
        mBundle.putDouble("Order_GasNum", order.getOrder_GasNum());

        mIntent.putExtras(mBundle);

        startActivity(mIntent);
    }

    //initList
    private void initList() {

        mAdapter = new OrderAdapter(MyOrdersActivity.this);

        mListView.setAdapter(mAdapter);
    }

    //initPtr
    private void initPtr() {

        mPtrFrame.setLastUpdateTimeRelateObject(this);

        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {

                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {

                //开始刷新
                beginToRefresh();

                mPtrFrame.refreshComplete();
            }
        });

        //可以设置自动刷新
//        mPtrFrame.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                //自动刷新
//            }
//        },1000);

    }

    //findView
    private void findView() {

        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.activity_my_orders_ptr);
        mListView = (ListView) findViewById(R.id.activity_my_orders_list);
    }

    //initToolbar
    private void initToolbar() {

        View rootView = findViewById(R.id.activity_my_orders_toolbar);

        if (rootView != null) {

            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

            toolbar.setTitle("我的订单");

            setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MyOrdersActivity.this.finish();
                }
            });
        }
    }

    //开始刷新
    private void beginToRefresh() {

        BmobQuery<Order> query = new BmobQuery<>("Order");

        if (MyApplication.isLoginSucceed) {

            String tel = MyApplication.mUser.getUser_Tel();

            query.addWhereMatches("User_Tel", tel);

            query.findObjects(MyOrdersActivity.this, new FindListener<Order>() {
                @Override
                public void onSuccess(List<Order> list) {

                    mDataList = list;

                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(MyOrdersActivity.this, "刷新成功!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int i, String s) {

                    Toast.makeText(MyOrdersActivity.this, "刷新失败,请稍后重试!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class OrderAdapter extends BaseAdapter {

        private Context context;

        private LayoutInflater layoutInflater;

        public OrderAdapter(Context context) {

            this.context = context;

            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = layoutInflater.inflate(R.layout.activity_my_orders_item, null);

            initConvertView(convertView, position);

            return convertView;
        }

        private void initConvertView(View convertView, final int position) {

            TextView mOrder_ID = (TextView) convertView.findViewById(R.id.activity_my_orders_textview_id);
            TextView mOrder_AllPrice = (TextView) convertView.findViewById(R.id.activity_my_orders_textview_all);
            TextView mOrder_Status = (TextView) convertView.findViewById(R.id.activity_my_orders_textview_status);
            final BoomMenuButton menuButton = (BoomMenuButton) convertView.findViewById(R.id.activity_my_orders_item_boom_ham);

            menuButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuButton.init(
                            drawables, // The drawables of images of sub buttons. Can not be null.
                            title,     // The texts of sub buttons, ok to be null.
                            colors,          // The colors of sub buttons, including pressed-state and normal-state.
                            ButtonType.HAM,        // The button type.
                            BoomType.PARABOLA,        // The boom type.
                            PlaceType.HAM_3_1,     // The place type.
                            null,                     // Ease type to move the sub buttons when showing.
                            null,                     // Ease type to scale the sub buttons when showing.
                            null,                     // Ease type to rotate the sub buttons when showing.
                            null,                     // Ease type to move the sub buttons when dismissing.
                            null,                     // Ease type to scale the sub buttons when dismissing.
                            null,                     // Ease type to rotate the sub buttons when dismissing.
                            null                      // Rotation degree.
                    );

                    menuButton.setSubButtonShadowOffset(
                            Util.getInstance().dp2px(2), Util.getInstance().dp2px(2));
                }
            }, 1);

            menuButton.setTag(new Integer(position));

            menuButton.setOnSubButtonClickListener(MyOrdersActivity.this);
            menuButton.setOnClickListener(new BoomMenuButton.OnClickListener() {
                @Override
                public void onClick() {

                    Integer i = (Integer) menuButton.getTag();
                    posi = i;

                    Log.e("i++", menuButton.getTag().toString());
                }
            });

            Order order = mDataList.get(mDataList.size() - position - 1);

            mOrder_ID.setText(order.getOrder_ID());

            Float price = order.getOrder_GasPrice();
            Float num = order.getOrder_GasNum();

            mOrder_AllPrice.setText(price * num + "");
            mOrder_Status.setText(order.getOrder_Status() + "");
        }
    }

    @Override
    public void onClick(int buttonIndex) {

        switch (buttonIndex) {

            case 0:


                break;

            case 1:

                toQRCode(posi);
                break;

            case 2:

                break;
        }
    }
}
