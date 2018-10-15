package layout.bigman.com.demo.other;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;


import java.util.List;
import java.util.Locale;

import layout.bigman.com.demo.R;

/**
 * @author 曾凡达
 * @date 2018/6/20
 */
public class MultiCatalogAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public static final int TYPE_LEVEL_TITLE = 0;
    public static final int TYPE_LEVEL_ITEM = 1;

    public MultiCatalogAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(TYPE_LEVEL_TITLE, R.layout.study_item_multi_catalog_title);
        addItemType(TYPE_LEVEL_ITEM, R.layout.study_item_single_catalog);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_TITLE:
                final CatalogTitbleBean titbleBean = (CatalogTitbleBean) item;
                helper.setText(R.id.tv_catalog_title, titbleBean.catalogTitle)
                        .setImageResource(R.id.iv_arrow, titbleBean.isExpanded() ? R.mipmap.study_ic_down_arrow : R.mipmap.study_ic_gray_up_arrow);
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = helper.getAdapterPosition();
                        if (titbleBean.isExpanded()) {
                            collapse(pos, true);
                        } else {
                            expand(pos,true);
                        }
                    }
                });
                break;
            case TYPE_LEVEL_ITEM:
                final CatalogItemBean catalogItemBean = (CatalogItemBean) item;

                TextView mCourseTank = helper.getView(R.id.tv_course_rank);
                if (helper.getLayoutPosition() == 1) {
                    helper.setBackgroundRes(R.id.fl_bg_icon, R.mipmap.study_ic_bg_playing);
                    helper.setImageResource(R.id.iv_course_type, R.mipmap.study_ic_ppt);
                    mCourseTank.setTextColor(ArmsUtils.getColor(mContext, R.color.base_main_color));
                    helper.setBackgroundRes(R.id.rl_container, R.drawable.shape_item_bg);
                } else {
                    helper.setBackgroundRes(R.id.fl_bg_icon, R.mipmap.study_ic_bg_normal);
                    helper.setImageResource(R.id.iv_course_type, R.mipmap.study_ic_catalog_play);
                    mCourseTank.setTextColor(ArmsUtils.getColor(mContext, R.color.text_color_666666));
                    helper.setBackgroundRes(R.id.rl_container, R.color.transparent);
                }

                if (helper.getLayoutPosition() < 10) {
                    mCourseTank.setText(String.format(Locale.CHINA,"%02d", helper.getLayoutPosition()));
                } else {
                    mCourseTank.setText(String.valueOf(helper.getLayoutPosition()));
                }
//                mCourseTank.setTypeface(ArmsUtils.getArialBlack(mContext));
                break;
        }
    }
}
