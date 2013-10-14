import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class HostTableModel extends AbstractTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Host","Port","Status","Total Failures","Total Successes","Total Connections"};
	private ArrayList<iHost> hostList = new ArrayList<iHost>();

	@Override
	public void addTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.hostList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		iHost h = (iHost) hostList.get(row);
		switch (col) {
			case 0:
				return h.getHost();
			case 1:
				return h.getPort();
			case 2:
				return h.getStatus();
			case 3:
				return h.getTotalConnectionFailures();
			case 4:
				return h.getTotalConnectionSuccess();
			case 5:
				return h.getTotalConenctionAttempts();
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
