package layout.bigman.com.demo.other;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;


/**
 * 目录标题类
 * @date 2018/6/20
 */
public class CatalogTitbleBean extends AbstractExpandableItem<CatalogItemBean> implements MultiItemEntity {

    public String catalogTitle ;

    public CatalogTitbleBean(String catalogTitle) {
        this.catalogTitle = catalogTitle;
    }

    @Override
    public int getLevel() {
        return MultiCatalogAdapter.TYPE_LEVEL_TITLE;
    }

    @Override
    public int getItemType() {
        return MultiCatalogAdapter.TYPE_LEVEL_TITLE;
    }
}
