package keksovmen.android.com.Implementation;

import android.text.InputFilter;
import android.text.Spanned;

public class NumberRangeFilter implements InputFilter {

    private final int min;
    private final int max;

    public NumberRangeFilter(int min, int max) {
        if (min <= max){
            this.min = min;
            this.max = max;
        }else {
            this.min = max;
            this.max = min;
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String result = dest.subSequence(0, dstart).toString();
        result += source.subSequence(start, end);
        result += dest.subSequence(dend, dest.length()).toString();

        try{
            if (inRange(Integer.parseInt(result)))
                return null;
        }catch (NumberFormatException ignored){
        }
        return "";
    }

    private boolean inRange(int val){
        return min <= val && val <= max;
    }
}
