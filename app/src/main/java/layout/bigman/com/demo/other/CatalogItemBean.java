package layout.bigman.com.demo.other;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;


/**
 * 目录Item类
 * @author 曾凡达
 * @date 2018/6/20
 */
public class CatalogItemBean extends AbstractExpandableItem implements MultiItemEntity {

    public String title ;
    public String time ;
    public String progress ;
    public String url ;
    public String total ;
    public String watchNum ;
    public int type ;
    public boolean isSelected;

    public CatalogItemBean() {
    }

    public CatalogItemBean(String url) {
        this.url = url;
    }

    public CatalogItemBean(String url, int type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public int getLevel() {
        return MultiCatalogAdapter.TYPE_LEVEL_ITEM;
    }

    @Override
    public int getItemType() {
        return MultiCatalogAdapter.TYPE_LEVEL_ITEM;
    }
}
