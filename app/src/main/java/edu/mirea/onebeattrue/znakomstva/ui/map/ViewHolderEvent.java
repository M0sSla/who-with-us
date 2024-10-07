package edu.mirea.onebeattrue.znakomstva.ui.map;

import androidx.recyclerview.widget.RecyclerView;

import edu.mirea.onebeattrue.znakomstva.databinding.ItemEventBinding;
import edu.mirea.onebeattrue.znakomstva.databinding.ItemMessageBinding;

public class ViewHolderEvent extends RecyclerView.ViewHolder{

    protected ItemEventBinding binding;

    public ViewHolderEvent(ItemEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}