package fi.jamk.saunaapp.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Abstract class, extend to create bindable {@link RecyclerView.ViewHolder}
 * @param <T>
 */
public abstract class BindableViewHolder<T> extends RecyclerView.ViewHolder {
    public BindableViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T binding);
}
