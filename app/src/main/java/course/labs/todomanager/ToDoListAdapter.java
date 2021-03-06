package course.labs.todomanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ToDoListAdapter extends BaseAdapter {

	private final List<ToDoItem> mItems = new ArrayList<ToDoItem>();
	private final Context mContext;
	private static final int CLOSE_DEADLINE_TIME = 86400000;

	private static final String TAG = "Lab-UserInterface";

	public ToDoListAdapter(Context context) {

		mContext = context;

	}

	// Добавляем  ToDoItem в адаптер
	// Уведомляем обсерверов, что данные изменились

	public void add(ToDoItem item) {

		mItems.add(item);
		notifyDataSetChanged();

	}

	public void edit(int position, Intent data) {
		ToDoItem item = mItems.get(position);
		if (item != null) {
			item.setFromIntent(data);
			notifyDataSetChanged();
		}
	}

	public void delete(int position) {
		mItems.remove(position);
		notifyDataSetChanged();
	}

	// Очищаем список адаптеров от всех элементов.

	public void clear() {

		mItems.clear();
		notifyDataSetChanged();

	}

	// Возвращает число элементов ToDoItems

	@Override
	public int getCount() {

		return mItems.size();

	}

	// Возвращает элемент ToDoItem в выбранной позиции

	@Override
	public Object getItem(int pos) {

		return mItems.get(pos);

	}

	// Получает ID для ToDoItem
	// В данном случае это всего лишь позиция

	@Override
	public long getItemId(int pos) {

		return pos;

	}

	// Создайте View для ToDoItem в определенной позиции
	// Не забудьте проверить, содержит ли выделенное convertView уже созданное  View
	// перед созданием нового View
	// Рассмотрите использование паттерна ViewHolder чтобы сделать скроллинг более эффективным.
	// См: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ToDoItem toDoItem = mItems.get(position);
		LayoutInflater  inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout itemLayout = (RelativeLayout) convertView;
		ViewHolder holder = null;
		if (itemLayout == null) {
			itemLayout = (RelativeLayout) inflater.from(parent.getContext()).inflate(R.layout.todo_item, parent ,false);
			holder = new ViewHolder();
			holder.titleView = (TextView) itemLayout.findViewById(R.id.titleView);
			holder.statusView = (CheckBox) itemLayout.findViewById(R.id.statusCheckBox);
			holder.priorityView = (TextView) itemLayout.findViewById(R.id.priorityView);
			holder.dateView = (TextView) itemLayout.findViewById(R.id.dateView);
			holder.warningView = (ImageView) itemLayout.findViewById(R.id.warningView);
			itemLayout.setTag(holder);
		} else {
			holder = (ViewHolder) itemLayout.getTag();
		}


		// Заполните специфичные данные ToDoItem
		// Помните, что данные, которые появляются в этом View
		// соответствуют пользовательскому интерфейсу, описанному
		// в файле макета

		holder.titleView.setText(toDoItem.getTitle());

		if(toDoItem.getStatus().equals(ToDoItem.Status.DONE)) {
			holder.statusView.setChecked(true);
			itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.done_background));
		} else {
			holder.statusView.setChecked(false);
			itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.primary));
		}

		holder.statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mItems.get(position).setStatus(isChecked ? ToDoItem.Status.DONE : ToDoItem.Status.NOTDONE);
						notifyDataSetChanged();
					}
				});


		String priority = "";
		switch (toDoItem.getPriority()) {
			case LOW:
				priority = mContext.getResources().getString(R.string.priority_low_string);
				break;
			case MED:
				priority =  mContext.getResources().getString(R.string.priority_medium_string);
				break;
			case HIGH:
				priority =  mContext.getResources().getString(R.string.priority_high_string);
				break;
		}
		holder.priorityView.setText(priority);

		holder.dateView.setText(ToDoItem.FORMAT.format(toDoItem.getDate()));
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long taskTime = toDoItem.getDate().getTime();
		if (!holder.statusView.isChecked()) {
			if (currentTime > taskTime) {
				holder.warningView.setVisibility(View.VISIBLE);
				holder.warningView.setImageResource(R.drawable.warning_red);
			} else if (Math.abs(currentTime - taskTime) < CLOSE_DEADLINE_TIME) {
				holder.warningView.setVisibility(View.VISIBLE);
				holder.warningView.setImageResource(R.drawable.warning_yellow);
			}
		}else {
			holder.warningView.setVisibility(View.INVISIBLE);
		}

		final ToDoManagerActivity activity = (ToDoManagerActivity) itemLayout.getContext();

		itemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.StartEditToDoItem(position, toDoItem);
			}
		});

		/*itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				return true;
			}
		});*/
		itemLayout.setOnCreateContextMenuListener( new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
											ContextMenu.ContextMenuInfo menuInfo) {
					/*AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
					menu.setHeaderTitle( ((ToDoItem) mItems.get(info.position)).getTitle());
					menu.add(Menu.NONE, 0, 0, "Delete");
					menu.add(Menu.NONE, 1, 1, "Edit");*/
			}
		});


		// Возвращает View которое только что создали
		return itemLayout;

	}


	public static class ViewHolder {
		public TextView titleView;
		public CheckBox statusView;
		public TextView priorityView;
		public TextView dateView;
		public ImageView warningView;
	}

}

