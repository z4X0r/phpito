package phpito.view.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import jaswt.canvas.CPUMonitorCanvas;
import jaswt.listener.selection.OpenerFileFromOSSelectionAdapter;
import jaswt.utils.Jaswt;
import jogger.Jogger;
import jutilas.utils.JutilasSys;
import phpito.core.PHPitoConf;
import phpito.core.PHPitoManager;
import phpito.data.Project;
import phpito.exception.ProjectException;
import phpito.exception.ServerException;
import phpito.view.listener.key.ProjectsTableKeyAdapter;
import phpito.view.listener.selection.FlushTableSelectionAdapter;
import phpito.view.listener.selection.TableSelectionAdapter;
import phpito.view.listener.selection.launcher.LauncherAboutSelctionAdapter;
import phpito.view.listener.selection.launcher.LauncherAddProjectSelectionAdapter;
import phpito.view.listener.selection.launcher.LauncherDeleteProjectSelectionListener;
import phpito.view.listener.selection.launcher.LauncherImportExportProjectsSelectionAdapter;
import phpito.view.listener.selection.launcher.LauncherSettingsProjectSelectionAdapter;
import phpito.view.listener.selection.launcher.LauncherSettingsSelctionAdapter;
import phpito.view.listener.selection.server.StartServerSelectionAdapter;
import phpito.view.listener.selection.server.StopServerSelectionAdapter;
import phpito.view.thread.WriterLogMonitorThread;
import swing2swt.layout.BorderLayout;

/**
 * Class Shell for principal PHPito shell
 * @author Andrea Serra
 *
 */
public class ShellPHPito extends Shell {
	private ShellPHPito shellPHPito;
	private Table table;
	private StyledText logOutText;
	private CPUMonitorCanvas cpuMonitorCanvas;
	private CLabel infoLabel;
//	private final int fontHeight = 20;
	private Long idProjectSelect;
	private boolean actvtLogMon;
	private boolean actvtSysInfo;
	private ArrayList<MenuItem> mntmStartList = new ArrayList<MenuItem>();
	private ArrayList<MenuItem> mntmStopList = new ArrayList<MenuItem>();
	private ArrayList<MenuItem> mntmProjectList = new ArrayList<MenuItem>();
	private ArrayList<Button> bttnStartList = new ArrayList<Button>();
	private ArrayList<Button> bttnStopList = new ArrayList<Button>();
	private ArrayList<Button> bttnProjectList = new ArrayList<Button>();
	private WriterLogMonitorThread writerLogMonitorThread;

	/* CONSTRUCT */
	public ShellPHPito(Display display) {
		super(display);
		this.shellPHPito = this;
		this.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					PHPitoManager.getInstance().getJoggerDebug().writeLog("Listener closing ShellPHPito");
					ArrayList<Project> projectsList = PHPitoManager.getInstance().getRunningProjects();
					if (!projectsList.isEmpty()) {
						PHPitoManager.getInstance().getJoggerDebug().writeLog("Servers are running");
						String msg = "Caution!!! Running server will be stopped.\nConfirm???";
						int resp = Jaswt.getInstance().launchMB(shellPHPito, SWT.YES | SWT.NO, "CAUTION!!!", msg);
						event.doit = resp == SWT.YES;
						if (event.doit) PHPitoManager.getInstance().stopAllRunningServer(); //for (Project project : projectsList) PHPitoManager.getInstance().stopServer(project);
					}
				} catch (IOException | ServerException | ProjectException e) {
					Jaswt.getInstance().launchMBError(shellPHPito, e, PHPitoManager.getInstance().getJoggerError());
				}
			}
		});
	}

	/* override to bypass check subclass error */
	@Override
	protected void checkSubclass() {
	}

	/* override open shell method */
	@Override
	public void open() {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("Open ShellPHPito");
		createContents();
		super.open();
		try {
			PHPitoManager.getInstance().flushRunningServers();
			flushTable();
			if (actvtLogMon) (writerLogMonitorThread = new WriterLogMonitorThread(this, PHPitoManager.getInstance().getReentrantLockLogServer())).start();
			if (actvtSysInfo) {
				cpuMonitorCanvas.setInfoLabel(infoLabel);
				cpuMonitorCanvas.start();
			}
		} catch (NumberFormatException | IOException | ProjectException e) {
			Jaswt.getInstance().launchMBError(shellPHPito, e, PHPitoManager.getInstance().getJoggerError());
		}
	}

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */

//	public int getFontHeight() {
//		return fontHeight;
//	}
	public StyledText getLogOutText() {
		return logOutText;
	}
	public Long getIdProjectSelect() {
		return idProjectSelect;
	}
	public String getIdProjectSelectString() {
		return String.valueOf(idProjectSelect);
	}
	public Table getTable() {
		return table;
	}
	public ArrayList<MenuItem> getMntmStartList() {
		return mntmStartList;
	}
	public ArrayList<MenuItem> getMntmStopList() {
		return mntmStopList;
	}
	public ArrayList<MenuItem> getMntmProjectList() {
		return mntmProjectList;
	}
	public ArrayList<Button> getBttnStartList() {
		return bttnStartList;
	}
	public ArrayList<Button> getBttnStopList() {
		return bttnStopList;
	}
	public ArrayList<Button> getBttnProjectList() {
		return bttnProjectList;
	}
	public CPUMonitorCanvas getCPUMonitorCanvas() {
		return cpuMonitorCanvas;
	}
	public CLabel getLblCPU() {
		return infoLabel;
	}
	public WriterLogMonitorThread getWriterLogMonitorThread() {
		return writerLogMonitorThread;
	}
	public void setWriterLogMonitorThread(WriterLogMonitorThread writerLogMonitorThread) {
		this.writerLogMonitorThread = writerLogMonitorThread;
	}
	
	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START PUBLIC METHODS */
	/* ################################################################################# */

	/* method that get project by select id */
	public Project getProjectSelect() {
		try {
			return PHPitoManager.getInstance().getProjectById(getIdProjectSelect());
		} catch (ProjectException e) {
			Jaswt.getInstance().launchMBError(this, e, PHPitoManager.getInstance().getJoggerError());
		}
		return null;
	}

	/* method that set id project selected to null */
	public void setIdProjectSelectToNull() {
		idProjectSelect = null;
	}
	
	/* method that flush the table and focus it */
	public void flushTableAndFocus() {
		try {
			flushTable();
		} catch (ProjectException e) {
			Jaswt.getInstance().launchMBError(shellPHPito, e, PHPitoManager.getInstance().getJoggerError());
		} finally {
			table.forceFocus();
		}
	}

	/* metodo che setta la variabile id con l'id progetto selezionato */
	public void autoSetIdProjectSelect() {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("ShellPHPito - Auto Set ID");
		Long id = null;
		if (table.getSelectionIndex() >= 0) id = Long.parseLong(table.getItem(table.getSelectionIndex()).getText(0));
		idProjectSelect = id;
		autoEnableButtonProject();
	}

	/* ################################################################################# */
	/* END PUBLIC METHODS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START PRIVATE METHODS */
	/* ################################################################################# */

	/* metodo che scrive tabella da hashmap di progetti */
	private void printProjectsOnTable(Map<String, Project> mapProjects) {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("ShellPHPito - Print Projects on Table");
		TableItem ti;
		Project p;
		table.removeAll();
		Set<String> keySet = mapProjects.keySet();
		for (String id : keySet) {
			p = mapProjects.get(id);
			ti = new TableItem(table, SWT.NONE);
			ti.setText(0, id);
			ti.setText(1, p.getName());
			ti.setText(2, p.getServer().getAddressAndPort());
			ti.setText(3, p.getServer().getPath());
			ti.setText(4, p.getServer().getStatePIDString());
			if (p.getServer().isRunning()) {
				ti.setBackground(new Color(getDisplay(), new RGB(0, 255, 0)));
				ti.setForeground(new Color(getDisplay(), new RGB(0, 0, 0)));
			}
		}
	}

	/* metodo che riscrive la tabella recuperando i dati dall'xml */
	private void flushTable() throws ProjectException {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("ShellPHPito - Flush Table");
		int indexTable = table.getSelectionIndex();
		Map<String, Project> mapProjects = PHPitoManager.getInstance().getReentrantLockProjectsXML().getProjectsMap();
		printProjectsOnTable(mapProjects);
		if (indexTable >= table.getItems().length || indexTable < 0) indexTable = 0;
		table.setSelection(indexTable);
		autoSetIdProjectSelect();
		autoEnableButtonProject();
	}

	/* metodo che abilita o disabilita i pulsanti legati a progetto */
	private void autoEnableButtonProject() {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("ShellPHPito - Enable Disabled Button of Project");
		Project project = shellPHPito.getProjectSelect();
		Boolean isRunnig = (project != null) ? project.getServer().isRunning() : null;
		if (isRunnig != null) {
			/* disable and enable button if server is running */ 
			for (Button bttn : shellPHPito.getBttnProjectList()) bttn.setEnabled(true);
			for (MenuItem mntm : shellPHPito.getMntmProjectList()) mntm.setEnabled(true);
			for (Button bttn : shellPHPito.getBttnStartList()) bttn.setEnabled(!isRunnig);
			for (MenuItem mntm : shellPHPito.getMntmStartList()) mntm.setEnabled(!isRunnig);
			for (Button bttn : shellPHPito.getBttnStopList()) bttn.setEnabled(isRunnig);
			for (MenuItem mntm : shellPHPito.getMntmStopList()) mntm.setEnabled(isRunnig);
		} else {
			/* disable and enable button if server is stopped */
			for (Button bttn : shellPHPito.getBttnProjectList()) bttn.setEnabled(false);
			for (MenuItem mntm : shellPHPito.getMntmProjectList()) mntm.setEnabled(false);
			for (Button bttn : shellPHPito.getBttnStartList()) bttn.setEnabled(false);
			for (MenuItem mntm : shellPHPito.getMntmStartList()) mntm.setEnabled(false);
			for (Button bttn : shellPHPito.getBttnStopList()) bttn.setEnabled(false);
			for (MenuItem mntm : shellPHPito.getMntmStopList()) mntm.setEnabled(false);
		}
	}

	/* metodo per creare contenuti */
	private void createContents() {
		PHPitoManager.getInstance().getJoggerDebug().writeLog("Create content ShellPHPito");
		this.setMinimumSize(300, 400);
		this.setSize(850, 640);
		this.setText("PHPito");
		this.setLayout(new BorderLayout(0, 0));
		Jaswt.getInstance().centerWindow(this);

		/* ################## START MENU BAR ################## */

		/* barra menu' della testata */
		Menu menu = new Menu(this, SWT.BAR);
		this.setMenuBar(menu);
		
		MenuItem mntm;
		Menu mn;
		
		/* menu' cascata progetti */
		mntm = new MenuItem(menu, SWT.CASCADE);
		mntm.setText("Projects");
		mn = new Menu(mntm);
		mntm.setMenu(mn);

		/* list of project menu */
		String[] menuProjectList = {"Add", "Settings", "Delete", "Start", "Stop", "Refresh table", "Import", "Export"};
		SelectionListener[] menuProjectSelAdptList = {
				new LauncherAddProjectSelectionAdapter(this),
				new LauncherSettingsProjectSelectionAdapter(this),
				new LauncherDeleteProjectSelectionListener(this),
				new StartServerSelectionAdapter(this),
				new StopServerSelectionAdapter(this),
				new FlushTableSelectionAdapter(this),
				new LauncherImportExportProjectsSelectionAdapter(this, LauncherImportExportProjectsSelectionAdapter.IMPORT),
				new LauncherImportExportProjectsSelectionAdapter(this, LauncherImportExportProjectsSelectionAdapter.EXPORT)
		};

		/* loop for project menu */
		for (int i = 0, length = menuProjectList.length; i < length; i++) {
			mntm = new MenuItem(mn, SWT.NONE);
			mntm.addSelectionListener(menuProjectSelAdptList[i]);
			mntm.setText(menuProjectList[i]);
			switch (menuProjectList[i]) {
				case "Delete":
				case "Settings":
					/* case add to project menu list */
					mntmProjectList.add(mntm);
					break;
				case "Start":
					/* case add to start server list */
					mntmStartList.add(mntm);
					break;
				case "Stop":
					/* case add to stop server list */
					mntmStopList.add(mntm);
					break;
				default:
					break;
			}
		}

		/* menu' cascata PHPito */
		mntm = new MenuItem(menu, SWT.CASCADE);
		mntm.setText("PHPito");
		mn = new Menu(mntm);
		mntm.setMenu(mn);

		/* list of PHPito menu */
		String[] menuPHPitoList = {"Settings PHPito", "Open log folder", "About"};
		SelectionListener[] menuPHPitoSelAdptList = {
				new LauncherSettingsSelctionAdapter(this),
				new OpenerFileFromOSSelectionAdapter(this, Jogger.getLogDirPath("server")),
				new LauncherAboutSelctionAdapter(this)
		};

		/* loop for PHPito menu */
		for (int i = 0, length = menuPHPitoList.length; i < length; i++) {
			mntm = new MenuItem(mn, SWT.NONE);
			mntm.addSelectionListener(menuPHPitoSelAdptList[i]);
			mntm.setText(menuPHPitoList[i]);
		}

		/* ################## END MENU BAR ################## */

		/* insert log monitor */
		GridData gd;
		actvtLogMon = PHPitoConf.getInstance().getActvtLogMonConf();
		if (actvtLogMon) {
			PHPitoManager.getInstance().getJoggerDebug().writeLog("Create Content ShellPHPito - Log Monitor ON");
			/* contenitore per la zona alta */
			Composite topComposite = new Composite(this, SWT.NONE);
			topComposite.setLayoutData(BorderLayout.NORTH);
			topComposite.setLayout(new GridLayout(1,  false));
			
			logOutText = new StyledText(topComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = 150;
			logOutText.setLayoutData(gd);
			logOutText.setForeground(PHPitoConf.getInstance().getColorForegrndLogMonConf());
			logOutText.setBackground(PHPitoConf.getInstance().getColorBckgrndLogMonConf());
			logOutText.setEditable(false);
			logOutText.setCursor(new Cursor(getDisplay(), SWT.CURSOR_CROSS));

		}

		/* contenitore per la zona laterale destra */
		ScrolledComposite rightScrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		rightScrolledComposite.setLayoutData(BorderLayout.WEST);
		rightScrolledComposite.setExpandHorizontal(true);
		rightScrolledComposite.setExpandVertical(true);
		Composite rightGridComposite = new Composite(rightScrolledComposite, SWT.NONE);
		rightGridComposite.setLayout(new GridLayout(1, false));

		/* impostazioni Grid Layout per larghezza pulsanti */
		GridData gdBttnWidth = new GridData();
		gdBttnWidth.widthHint = 150;

		/* impostazioni Grid Layout per altezza label(spazio) tra pulsanti */
		GridData gdLblHeight = new GridData();
		gdLblHeight.heightHint = 13;

		/* loop for lateral buttons */
		Button bttn;
		for (int i = 0, length = menuProjectList.length - 3; i < length; i++) {
			bttn = new Button(rightGridComposite, SWT.PUSH);
			bttn.addSelectionListener(menuProjectSelAdptList[i]);
			bttn.setLayoutData(gdBttnWidth);
			bttn.setText(menuProjectList[i]);
			bttn.setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
			new Label(rightGridComposite, SWT.NONE).setLayoutData(gdLblHeight);
			switch (menuProjectList[i]) {
				case "Delete":
				case "Settings":
					/* case add to project button list */
					bttnProjectList.add(bttn);
					break;
				case "Start":
					/* case add to start button list */
					bttnStartList.add(bttn);
					break;
				case "Stop":
					/* case add to stop button list */
					bttnStopList.add(bttn);
					break;
				default:
					break;
			}
		}

		/* insert content on scroll */
		rightScrolledComposite.setContent(rightGridComposite);
		rightScrolledComposite.setMinSize(rightGridComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		/* composite centrale con scroll verticale */
		ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(BorderLayout.CENTER);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		/* tabella */
		TableViewer tableViewer = new TableViewer(scrolledComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		scrolledComposite.setContent(table);
		scrolledComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		table.addSelectionListener(new TableSelectionAdapter(this));
		table.addKeyListener(new ProjectsTableKeyAdapter(this));
		table.forceFocus();

		/* poupup menu per tabella */
		Menu ppmnTbl = new Menu(table);
		table.setMenu(ppmnTbl);

		/* loop for popup menu in table */
		for (int i = 0, length =  menuProjectList.length; i < length; i++) {
			mntm = new MenuItem(ppmnTbl, SWT.NONE);
			mntm.addSelectionListener(menuProjectSelAdptList[i]);
			mntm.setText(menuProjectList[i]);
			switch (menuProjectList[i]) {
				case "Delete":
				case "Settings":
					/* case add to project menu list */
					mntmProjectList.add(mntm);
					break;
				case "Start":
					/* case add to start server list */
					mntmStartList.add(mntm);
					break;
				case "Stop":
					/* case add to stop server list */
					mntmStopList.add(mntm);
					break;
				default:
					break;
			}
		}

		/* nomi e stili delle colonne per la tabella*/
		String[] titles = { "id", "Name", "Address", "Path", "Satus"};
		int[] width = {40, 100, 130, 260, 100};
		int[] style = {SWT.CENTER, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.CENTER};

		/* loop for headers table */
		TableViewerColumn tblclmn;
		for (int i = 0, length = titles.length; i < length; i++) {
			tblclmn = new TableViewerColumn(tableViewer, style[i]);
			tblclmn.getColumn().setText(titles[i]);
			tblclmn.getColumn().setWidth(width[i]);
			tblclmn.getColumn().setResizable(true);
			tblclmn.getColumn().setMoveable(true);
		}

		/* draw sys info */
		actvtSysInfo = PHPitoConf.getInstance().getActvtSysInfoConf();
		if (actvtSysInfo) {
			PHPitoManager.getInstance().getJoggerDebug().writeLog("Create Content ShellPHPito - Sys Info ON");
			Composite compositeBottom = new Composite(shellPHPito, SWT.NONE);
			compositeBottom.setLayoutData(BorderLayout.SOUTH);

			/* draw CPU monitor */
			int xInfoLabel = 25;
			if (PHPitoConf.getInstance().getActvtCPUMon()) {
				PHPitoManager.getInstance().getJoggerDebug().writeLog("Create Content ShellPHPito - CPU Monitor ON");
				cpuMonitorCanvas = new CPUMonitorCanvas(compositeBottom);
				cpuMonitorCanvas.setBounds(20, 20, 80, 60);
				cpuMonitorCanvas.setCPUMonStyle(PHPitoConf.getInstance().getStyleLogMonConf());
				xInfoLabel = 110;
			}

			/* draw other info */
			if (PHPitoConf.getInstance().getOthInfo()) {
				PHPitoManager.getInstance().getJoggerDebug().writeLog("Create Content ShellPHPito - Other Info ON");
				infoLabel = new CLabel(compositeBottom, SWT.NONE);
				infoLabel.setBounds(xInfoLabel, 15, 200, 70);
				infoLabel.setText(JutilasSys.getInstance().getSystemInfo(null));
			}
			new Label(compositeBottom, SWT.NONE).setBounds(0, 100, 300, 0);
		}
	}

	/* ################################################################################# */
	/* END PRIVATE METHODS */
	/* ################################################################################# */

}
