package edu.fandm.research.vpnplus.Application.AppInterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.fandm.research.vpnplus.Application.Database.DataLeak;
import edu.fandm.research.vpnplus.R;

/**
 * Created by justinhu on 16-03-13.
 */
public class DetailListViewAdapter extends BaseAdapter {
    private final Context context;
    private List<DataLeak> list;

    public DetailListViewAdapter(Context context, List<DataLeak> list) {
        super();
        this.context = context;
        this.list = list;

    }

    public void updateData(List<DataLeak> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_detail, null);
            holder = new ViewHolder();

            holder.type = (TextView) convertView.findViewById(R.id.detail_type);
            holder.time = (TextView) convertView.findViewById(R.id.detail_time);
            holder.classification = (TextView) convertView.findViewById(R.id.detail_classification);
            holder.destination = (TextView) convertView.findViewById(R.id.detail_destination);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DataLeak leak = list.get(position);
        holder.type.setText(leak.getType());
        holder.time.setText(leak.getTimestamp());
        holder.classification.setText(leak.getLeakClassification());
        holder.destination.setText(leak.getDestination());
        return convertView;
    }

    public static class ViewHolder {
        public TextView type;
        public TextView classification;
        public TextView time;
        public TextView destination;
    }
}
