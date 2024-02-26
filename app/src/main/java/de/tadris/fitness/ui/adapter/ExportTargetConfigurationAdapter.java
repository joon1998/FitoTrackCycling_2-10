package de.tadris.fitness.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.data.ExportTargetConfiguration;
import de.tadris.fitness.util.autoexport.target.ExportTarget;

public class ExportTargetConfigurationAdapter extends RecyclerView.Adapter<ExportTargetConfigurationAdapter.ExportTargetConfigurationViewHolder> {

    public static class ExportTargetConfigurationViewHolder extends RecyclerView.ViewHolder {
        final View root;
        final TextView nameText;
        final TextView descriptionText;
        final View deleteButton;

        ExportTargetConfigurationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            nameText = itemView.findViewById(R.id.targetTitle);
            descriptionText = itemView.findViewById(R.id.targetDescription);
            deleteButton = itemView.findViewById(R.id.targetDelete);
        }
    }

    private final List<ExportTargetConfiguration> targets;
    private final ExportTargetAdapterListener listener;

    public ExportTargetConfigurationAdapter(List<ExportTargetConfiguration> targets, ExportTargetAdapterListener listener) {
        this.targets = targets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExportTargetConfigurationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_export_target, parent, false);
        return new ExportTargetConfigurationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExportTargetConfigurationViewHolder holder, int position) {
        ExportTargetConfiguration configuration = targets.get(position);
        ExportTarget targetImpl = configuration.getTargetImplementation();

        if (targetImpl == null) {
            holder.nameText.setText(configuration.type);
        } else {
            holder.nameText.setText(targetImpl.getTitleRes());
        }
        holder.descriptionText.setText(configuration.data);
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(configuration));
    }

    @Override
    public int getItemCount() {
        return targets.size();
    }

    public interface ExportTargetAdapterListener {

        void onDelete(ExportTargetConfiguration configuration);

    }

}
