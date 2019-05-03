package it.phpito.view.listener.selection;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.phpito.view.listener.selection.luncher.LuncherModifyProjectSelectionAdapter;
import it.phpito.view.shell.ShellPHPito;

public class TableSelectionAdapter extends SelectionAdapter {
	private ShellPHPito shellPHPito;

	public TableSelectionAdapter(ShellPHPito shellPHPito) {
		super();
		this.shellPHPito = shellPHPito;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		new LuncherModifyProjectSelectionAdapter(shellPHPito).lunchModifyProject();;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		shellPHPito.autoSetIdProjectSelect();
	}
}
