package MarcusD.TerraInvedit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import MarcusD.TerraInvedit.ItemRegistry.Buff;
import MarcusD.TerraInvedit.ItemRegistry.ItemEntry;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class InveditActivity extends ListActivity {

	ArrayList<Item> listItems=new ArrayList<Item>();
	
	byte[] buf;
	String abs;
	ItemRegistry ir;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<Item> adapter;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.activity_items);
        adapter=new ItemArrayAdapter(this, listItems);
        setListAdapter(adapter);
        Intent it = getIntent();
        buf = it.getByteArrayExtra("INV");
        abs = it.getStringExtra("ABSINV");
        ir = ItemRegistry.instance;	
        refresh();
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    public void refresh()
    {
    	listItems.clear();
    	for(int i = 0; i < 40; i++)
        {
    		ByteBuffer bb = ByteBuffer.allocate(2);
    		bb.order(ByteOrder.LITTLE_ENDIAN);
    		bb.put(buf[(i * 5) + 89]);
    		bb.put(buf[(i * 5) + 90]);
    		short iid = bb.getShort(0);
    		bb.clear();
    		bb.put(buf[(i * 5) + 91]);
    		bb.put(buf[(i * 5) + 92]);
    		short cnt = bb.getShort(0);
    		ItemEntry ier = ir.basemap.get(iid);
    		if(ier.iid != iid) Log.e("disc", "loaded "+ String.format("%04X", iid) + ", got " + String.format("%04X", ier.iid) + ", plus iid is "+ String.format("%02X", buf[(i * 5) + 90]) + String.format("%02X", buf[(i * 5) + 89]));
            listItems.add(new Item(ier, cnt, Buff.n(buf[(i * 5) + 93])));
        }
        adapter.notifyDataSetChanged();
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		final Item i = (Item)getListView().getItemAtPosition(position);
		
		Intent it = new Intent(getApplicationContext(), ActivityItemedit.class);
		it.putExtra("ID", i.item.iid);
		it.putExtra("POS", position);
		it.putExtra("CNT", i.cnt);
		it.putExtra("BUF", (byte)i.buffs.ordinal());
		startActivityForResult(it, 666);
	}
    
    @Override
    protected void onActivityResult(int req, int res, final Intent dat)
    {
    	if(req == 666)
    	{
    		if(res == 420)
    		{
    			int pos = dat.getIntExtra("POS", -1);
    			ByteBuffer bb = ByteBuffer.allocate(2);
    			bb.order(ByteOrder.LITTLE_ENDIAN);
    			bb.putShort(dat.getShortExtra("ID", (short)0));
    			buf[(pos * 5) + 89] = bb.get(0);
    			buf[(pos * 5) + 90] = bb.get(1);
    			bb.clear();
    			bb.putShort(dat.getShortExtra("CNT", (short)0));
    			buf[(pos * 5) + 91] = bb.get(0);
    			buf[(pos * 5) + 92] = bb.get(1);
    			buf[(pos * 5) + 93] = dat.getByteExtra("BUF", (byte)0);
    			refresh();
    		}
    	}
    	else
    	{
    		super.onActivityResult(req, res, dat);
    	}
    }
	
	public class Item
	{
		public Item(ItemEntry iem, short c, Buff b)
		{
			this.item = iem;
			this.cnt = c;
			this.buffs = b;
		}
		public ItemEntry item;
		public short cnt;
		public Buff buffs;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_invedit, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Log.d("option", item.getItemId() + " " + item.getTitle());
		 switch (item.getItemId())
		 {
	        case R.id.item1:
	        	Intent i = new Intent();
	        	i.putExtra("INV", buf);
	        	i.putExtra("ABSINV", getIntent().getStringExtra("ABSINV"));
	        	setResult(69, i);
	        	finish();
	            return true;
	        case R.id.item2:
	        	setResult(-1);
	        	finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public class ItemArrayAdapter extends ArrayAdapter<Item> {
		private final Context context;
		private final ArrayList<Item> values;
	 
		public ItemArrayAdapter(Context context, ArrayList<Item> values) {
			super(context, R.layout.list_items, values);
			this.context = context;
			this.values = values;
		}


		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.list_items, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
			Item it = values.get(position);
			if(it == null) return rowView;
			if(it.item == null)
			{
				textView.setText((it.buffs == Buff._ ? "" : it.buffs.toString()) + " " + getString(R.string.javatrans_missingno) + " " + it.cnt + " (???)");
				return rowView;
			}
			textView.setText((it.buffs == Buff._ ? "" : it.buffs.toString()) + " " + it.item.iname + " " + it.cnt + " (" + it.item.iid + ")");	 
			if(it.item.img != null) imageView.setImageBitmap(it.item.img);
	 
			return rowView;
		}
	}
}
