package com.kezy.test.weight;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.kezy.test.R;


/**
 * @Description 游戏中心, 悬浮挂件
 */
public class LetoGameCenterFloatTipView extends FrameLayout implements View.OnClickListener {


    private LinearLayout llActivity;
    private ImageView ivActivity1;
    private ImageView ivActivity2;
    private ImageView ivActivity3;

    private ImageView ivBottom;


    public LetoGameCenterFloatTipView(@NonNull Context context) {
        super(context);
        init(context);
    }


    public LetoGameCenterFloatTipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LetoGameCenterFloatTipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LetoGameCenterFloatTipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_gamead_float_tips_layout, (ViewGroup) getRootView());

        llActivity = view.findViewById(R.id.game_float_activity_layout);
        ivActivity1 = view.findViewById(R.id.game_activity_1);
        ivActivity1.setOnClickListener(this::onClick);
        ivActivity2 = view.findViewById(R.id.game_activity_2);
        ivActivity2.setOnClickListener(this::onClick);
        ivActivity3 = view.findViewById(R.id.game_activity_3);
        ivActivity3.setOnClickListener(this::onClick);
        ivBottom = view.findViewById(R.id.game_float_bottom_pen);
        ivBottom.setOnClickListener(this::onClick);

        initData();

    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.game_activity_1) {
            Toast.makeText(getContext(), "11111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.game_activity_2) {
            Toast.makeText(getContext(), "22222", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.game_activity_3) {
            Toast.makeText(getContext(), "333333", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.game_float_bottom_pen) {
            if (llActivity.getVisibility() == VISIBLE) {
                hideActivity();
            } else {
                showActivity();
            }
        }
    }

    public void showActivity() {
        llActivity.setVisibility(VISIBLE);
    }

    public void hideActivity() {
        llActivity.setVisibility(GONE);
    }
}
