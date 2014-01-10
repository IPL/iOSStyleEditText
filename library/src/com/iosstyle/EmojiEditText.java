package com.iosstyle;

import java.nio.charset.Charset;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class EmojiEditText extends EditText {

    private static final String TAG = EmojiEditText.class.getName();
    private static final String START_CHAR = "[";
    private static final String END_CHAR = "]";

    private InputFilter filter;

    public EmojiEditText(Context context) {
        super(context);
        init();
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                    Spanned dest, int dstart, int dend) {
                if (TextUtils.isEmpty(source))
                    return null;
                byte[] bytes = source.toString().getBytes();
                String hexStr = convert(bytes);
                try {
                    Resources resources = getContext().getResources();
                    int id = resources.getIdentifier("emoji_" + hexStr,
                            "drawable", getContext().getPackageName());
                    if (id != 0) {
                        return START_CHAR + hexStr + END_CHAR;
                    }
                } catch (Exception e) {
                    return null;
                }
                return null;
            }
        };
        this.setFilters(new InputFilter[] { filter });
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i,
                    int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                emotifySpannable(editable);

                String rawContent = editable.toString();

                SpannableString spannable = new SpannableString(rawContent);
                emotifySpannable(spannable);
            }
        });
    }

    public static String convert(byte[] bytes) {
        try {
            String str = new String(bytes, Charset.forName("UTF-8"));
            int[] result = toCodePointArray(str);
            for (int i = 0; i < result.length; i++) {
                String hex_result = Integer.toHexString(result[i]);
            }
            int codePoint = str.codePointAt(0);
            String hex_result = Integer.toHexString(codePoint);
            return hex_result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static int[] toCodePointArray(String str) {
        char[] ach = str.toCharArray();
        int len = ach.length;
        int[] acp = new int[Character.codePointCount(ach, 0, len)];
        int j = 0;
        for (int i = 0, cp; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(ach, i);
            acp[j++] = cp;
        }
        return acp;
    }

    /**
     * Work through the contents of the string, and replace any occurrences of
     * [icon] with the imageSpan
     * 
     * @param spannable
     */
    private void emotifySpannable(Spannable spannable) {
        int length = spannable.length();
        int position = 0;
        int tagStartPosition = 0;
        int tagLength = 0;
        StringBuilder buffer = new StringBuilder();
        boolean inTag = false;

        if (length <= 0)
            return;

        do {
            String c = spannable.subSequence(position, position + 1).toString();

            if (!inTag && c.equals(START_CHAR)) {
                buffer = new StringBuilder();
                tagStartPosition = position;
                Log.d(TAG, "   Entering tag at " + tagStartPosition);

                inTag = true;
                tagLength = 0;
            }

            if (inTag) {
                buffer.append(c);
                tagLength++;

                // Have we reached end of the tag?
                if (c.equals(END_CHAR)) {
                    inTag = false;

                    String tag = buffer.toString();
                    int tagEnd = tagStartPosition + tagLength;

                    Log.d(TAG, "Tag: " + tag + ", started at: "
                            + tagStartPosition + ", finished at " + tagEnd
                            + ", length: " + tagLength);

                    String hexStr = tag.substring(1, tag.length() - 1);
                    try {
                        int id = getContext().getResources().getIdentifier(
                                "emoji_" + hexStr, "drawable", getContext().getPackageName());
                        Drawable emoji = getContext().getResources()
                                .getDrawable(id);
                        emoji.setBounds(0, 0, this.getLineHeight(),
                                this.getLineHeight());
                        ImageSpan imageSpan = new ImageSpan(emoji,
                                ImageSpan.ALIGN_BOTTOM);
                        spannable.setSpan(imageSpan, tagStartPosition, tagEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception e) {
                    }
                }
            }

            position++;
        } while (position < length);
    }

}
