/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package layout.bigman.com.demo.other;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;


/**
 * ================================================
 * 一些框架常用的工具
 * <p>
 * Created by JessYan on 2015/11/23.
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ArmsUtils {

    private ArmsUtils() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    /**
     * 获得资源
     */
    public static Resources getResources(Context context) {
        return context.getResources();
    }

    /**
     * 从String 中获得字符
     *
     * @return
     */
    public static String getString(Context context, int stringId) {
        return getResources(context).getString(stringId);
    }

    /**
     * 从String 中获得字符
     *
     * @return
     */
    public static String getString(Context context, int stringId,Object...formatArgs ) {
        return getResources(context).getString(stringId, formatArgs);
    }

    /**
     * 获得颜色
     */
    public static int getColor(Context context, int colorId) {
        return getResources(context).getColor(colorId);
    }

    /**
     * 获得drawable
     */
    public static Drawable getDrawable(Context context, int drawableId) {
        return getResources(context).getDrawable(drawableId);
    }

    public static Typeface getArialBlack(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/ArialBlack.ttf");
    }





}
