package seostudio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Label;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initApp();
			}
		});
	}
	
	private static void initApp() {
		long initTime = System.currentTimeMillis();
		String url = "http://www.masterbranch.com/";
		Crawler c = new Crawler(url, Pattern.quote(url)+"(.*?)");
		int max = 5;
		for(int i=0; i<=max; i++) {
			c.browse(i);
		}
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime - initTime;
		
		JFrame frame = new JFrame("Results");
		frame.getContentPane().add(new Label("Indexed pages: " + c.getIndexedPages() 
				+ ". Indexed and noflow: " + c.getIndexedNoFollowPages() + ". Total pages: "
				+ c.getResults().size() + ". Connection errors: " + c.getConnectionErrors()
				+ ". Pages with SEO error: " + c.getSeoErrors() + ". Time spent (total minutes): " + (int)(elapsedTime/60000)
				+ ". Time spent (per page ms): " + (elapsedTime/c.getResults().size())) , BorderLayout.NORTH);
		JTable table = new JTable(new ResultTableModel(c.getResults().values(), url.length()-1));
		frame.getContentPane().add(new JScrollPane(table));
		
		frame.setSize(1200, 600);
		frame.setLocationRelativeTo(null);

		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(20);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(3).setPreferredWidth(200);
		table.getColumnModel().getColumn(4).setPreferredWidth(200);
		table.getColumnModel().getColumn(5).setPreferredWidth(200);
		table.getColumnModel().getColumn(6).setPreferredWidth(200);
		table.getColumnModel().getColumn(7).setPreferredWidth(20);
		table.getColumnModel().getColumn(8).setPreferredWidth(20);
		
		table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				setBackground(Color.WHITE);
				
				setToolTipText(null);
				if(value != null) {
					setBackground(Color.RED);
					setToolTipText(value.toString());
				}
				return this;
			}
		});
		
		table.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				setBackground(Color.WHITE);
				
				setToolTipText(null);
				if(value != null) {
					setBackground(Color.RED);
					setToolTipText(value.toString());
				}
				return this;
			}
		});
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}

class ResultTableModel implements TableModel {

	private final String[] columns = new String[]{ "URL",
			"D", "L", "Title", "<h1>", "Description",
			"Keywords", "I", "F", "Error", "SEO Errors" };
	private final Class<?>[] types = new Class<?>[]{ String.class,
			Integer.class, Integer.class, String.class, String.class, String.class,
			String.class, Boolean.class, Boolean.class, String.class, String.class };

	private List<Result> results;
	private int n;

	public ResultTableModel(Collection<Result> results, int n) {
		this.results = new ArrayList<Result>(results);
		for (Iterator<Result> iterator = this.results.iterator(); iterator.hasNext();) {
			Result result = iterator.next();
			if(!result.visited) {
				iterator.remove();
			}
		}
		Collections.sort(this.results, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				return o2.links - o1.links;
			}
		});
		this.n = n;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public int getRowCount() {
		return results.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Result r = results.get(rowIndex);
		switch(columnIndex) {
		case 0:
			return r.url.substring(n);
		case 1:
			return r.depth;
		case 2:
			return r.links;
		case 3:
			return r.title;
		case 4:
			return r.h1;
		case 5:
			return r.description;
		case 6:
			return r.keywords;
		case 7:
			return r.index;
		case 8:
			return r.follow;
		case 9:
			return r.error;
		case 10:
			return r.seoError;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

}
