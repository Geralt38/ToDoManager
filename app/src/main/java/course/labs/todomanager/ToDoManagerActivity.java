package course.labs.todomanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import course.labs.todomanager.ToDoItem.Priority;
import course.labs.todomanager.ToDoItem.Status;

public class ToDoManagerActivity extends ListActivity {

	private static final int ADD_TODO_ITEM_REQUEST = 0;
	private static final int EDIT_TODO_ITEM_REQUEST = 1;
	private static final String FILE_NAME = "TodoManagerActivityData.txt";
	private static final String TAG = "Lab-UserInterface";

	// ID для элментов меню
	private static final int MENU_DELETE = Menu.FIRST;
	private static final int MENU_DUMP = Menu.FIRST + 1;

	private static int selectedItem = 0;

	ToDoListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Создает новый TodoListAdapter для данного ListView
		mAdapter = new ToDoListAdapter(getApplicationContext());

		// Положить разделитель между ToDoItems и FooterView
		getListView().setFooterDividersEnabled(true);

		TextView footerView = (TextView) ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);
		getListView().addFooterView(footerView);

		footerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StartAddToDoItem();
			}
		});

		getListView().setAdapter(mAdapter);
		registerForContextMenu(getListView());
	}

	public void StartAddToDoItem() {
		Intent addToDoItemIntent = new Intent(this, AddToDoActivity.class);

		startActivityForResult(addToDoItemIntent, ADD_TODO_ITEM_REQUEST);
	}

	public void StartEditToDoItem(int position, ToDoItem toDoItem) {
		Intent editToDoItemIntent = new Intent(this, AddToDoActivity.class);
		ToDoItem.packageIntent(editToDoItemIntent,toDoItem.getTitle(),toDoItem.getPriority(),toDoItem.getStatus(),"");
		editToDoItemIntent.putExtra("position", position);
		editToDoItemIntent.putExtra("edit", true);
		editToDoItemIntent.putExtra("date",ToDoItem.DATE_FORMAT.format(toDoItem.getDate()));
		editToDoItemIntent.putExtra("time",ToDoItem.TIME_FORMAT.format(toDoItem.getDate()));
		/*editToDoItemIntent.putExtra("year",toDoItem.getDate().getYear());
		editToDoItemIntent.putExtra("month",toDoItem.getDate().getMonth());
		editToDoItemIntent.putExtra("day",toDoItem.getDate().getDay());
		editToDoItemIntent.putExtra("hours",toDoItem.getDate().getHours());
		editToDoItemIntent.putExtra("minutes",toDoItem.getDate().getMinutes());*/

		startActivityForResult(editToDoItemIntent, EDIT_TODO_ITEM_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG,"Entered onActivityResult()");

		if (requestCode == ADD_TODO_ITEM_REQUEST) {
			if (resultCode == RESULT_OK) {
				ToDoItem newItem = new ToDoItem(data);
				mAdapter.add(newItem);
			}
		} else if (requestCode == EDIT_TODO_ITEM_REQUEST) {
			if (resultCode == RESULT_OK) {
				mAdapter.edit(data.getIntExtra("position", 0), data);
			}
		}

	}

	// Не изменяйте все что ниже

	@Override
	public void onResume() {
		super.onResume();

		// Загрузить сохраненные ToDoItems, при необходимости

		if (mAdapter.getCount() == 0)
			loadItems();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Сохранить ToDoItems

		saveItems();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete all");
		menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			mAdapter.clear();
			return true;
		case MENU_DUMP:
			dump();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void dump() {

		for (int i = 0; i < mAdapter.getCount(); i++) {
			String data = ((ToDoItem) mAdapter.getItem(i)).toLog();
			Log.i(TAG,	"Item " + i + ": " + data.replace(ToDoItem.ITEM_SEP, ","));
		}

	}

	// Загрузка сохраненных ToDoItems
	private void loadItems() {
		BufferedReader reader = null;
		try {
			FileInputStream fis = openFileInput(FILE_NAME);
			reader = new BufferedReader(new InputStreamReader(fis));

			String title = null;
			String priority = null;
			String status = null;
			Date date = null;

			while (null != (title = reader.readLine())) {
				priority = reader.readLine();
				status = reader.readLine();
				date = ToDoItem.FORMAT.parse(reader.readLine());
				mAdapter.add(new ToDoItem(title, Priority.valueOf(priority),
						Status.valueOf(status), date));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Сохранение ToDoItems в файл
	private void saveItems() {
		PrintWriter writer = null;
		try {
			FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					fos)));

			for (int idx = 0; idx < mAdapter.getCount(); idx++) {

				writer.println(mAdapter.getItem(idx));

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo) {
		if (v.getId()== getListView().getId()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle( ((ToDoItem) mAdapter.getItem(info.position)).getTitle());
			menu.add(Menu.NONE, 0, 0, "Edit");
			menu.add(Menu.NONE, 1, 1, "Delete");
			selectedItem = info.position;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		switch (menuItemIndex) {
			case 0: {
				this.StartEditToDoItem(selectedItem, (ToDoItem) mAdapter.getItem(selectedItem));
				break;
			}
			case 1: {
				mAdapter.delete(selectedItem);
				break;
			}
		}
		return true;
	}
}