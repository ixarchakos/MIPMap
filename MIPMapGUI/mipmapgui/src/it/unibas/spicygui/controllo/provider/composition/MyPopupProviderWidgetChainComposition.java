/*
Copyright (C) 2007-2011  Database Group - Universita' della Basilicata
Giansalvatore Mecca - giansalvatore.mecca@unibas.it
Salvatore Raunich - salrau@gmail.com
Marcello Buoncristiano - marcello.buoncristiano@yahoo.it

This file is part of ++Spicy - a Schema Mapping and Data Exchange Tool

++Spicy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

++Spicy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ++Spicy.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicygui.controllo.provider.composition;

import it.unibas.spicy.model.exceptions.IllegalDataSourceException;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.persistence.DAOException;
import it.unibas.spicy.persistence.xml.DAOXsd;
import it.unibas.spicy.utility.SpicyEngineConstants;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.Utility;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.controllo.Scenarios;
import it.unibas.spicygui.controllo.composition.MutableMappingTask;
import it.unibas.spicygui.controllo.window.ActionProjectTree;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.widget.ChainWidget;
import it.unibas.spicygui.widget.ICompositionWidget;
import it.unibas.spicygui.widget.caratteristiche.AbstractCaratteristicheWidgetComposition;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetChainComposition;
import it.unibas.spicygui.widget.caratteristiche.ConnectionInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

public class MyPopupProviderWidgetChainComposition implements PopupMenuProvider, ActionListener {

    private static Log logger = LogFactory.getLog(MyPopupProviderWidgetChainComposition.class);
    private DAOXsd daoXsd = new DAOXsd();
    private Vista vista;
    private Modello modello;
    private ActionProjectTree actionProjectTree;
    private JPopupMenu menu;
    private boolean first = true;
    private ChainWidget chainWidget;
    private JMenuItem itemLoadDataSource;
    private LayerWidget connectionLayer;
    private final String SET_ROOT = "set root";
    private final String LOAD = "load";
    private final String DELETE = "delete";

    public MyPopupProviderWidgetChainComposition(Scene scene, LayerWidget connectionLayer) {
        this.connectionLayer = connectionLayer;
        this.executeInjection();
        this.createPopupMenu();
    }

    public JPopupMenu getPopupMenu(Widget widget, Point arg1) {
        if (first) {
            chainWidget = (ChainWidget) widget;
            first = false;
        }
        LayerWidget mainLayer = (LayerWidget) chainWidget.getParentWidget();
        CaratteristicheWidgetChainComposition caratteristicheWidgetChainComposition = (CaratteristicheWidgetChainComposition) mainLayer.getChildConstraint(chainWidget);
        ICompositionWidget sourceWidget = caratteristicheWidgetChainComposition.getSourceScenario();
        itemLoadDataSource.setEnabled(sourceWidget != null);
        return menu;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(SET_ROOT)) {
        } else if (e.getActionCommand().equals(DELETE)) {
            deleteWidget();
        } else {
            loadDataSource();

        }
    }

    private void createPopupMenu() {
        menu = new JPopupMenu("Popup menu");

        JMenuItem itemDeleteWidget;

        itemDeleteWidget = new JMenuItem(NbBundle.getMessage(Costanti.class, Costanti.DELETE_WIDGET_COMPOSITION));
        itemDeleteWidget.setActionCommand(DELETE);
        itemDeleteWidget.addActionListener(this);
        menu.add(itemDeleteWidget);

        itemLoadDataSource = new JMenuItem(NbBundle.getMessage(Costanti.class, Costanti.LOAD_DATASOURCE_FOR_CHAIN));
        itemLoadDataSource.setActionCommand(LOAD);
        itemLoadDataSource.addActionListener(this);
        menu.add(itemLoadDataSource);
    }

    private IDataSourceProxy loadXMLDataSource(String schemaPath) throws DAOException {
        IDataSourceProxy dataSource = daoXsd.loadSchema(schemaPath);
        return dataSource;
    }

    private void loadDataSource() {
        JFileChooser chooser = vista.getFileChooserApriXSD();
        try {
            LayerWidget mainLayer = (LayerWidget) chainWidget.getParentWidget();
            CaratteristicheWidgetChainComposition caratteristicheWidgetChainComposition = (CaratteristicheWidgetChainComposition) mainLayer.getChildConstraint(chainWidget);
            ICompositionWidget sourceWidget = caratteristicheWidgetChainComposition.getSourceScenario();
            AbstractCaratteristicheWidgetComposition caratteristicheSourceWidget = (AbstractCaratteristicheWidgetComposition) mainLayer.getChildConstraint((Widget) sourceWidget);
            MappingTask mappingTaskSource = caratteristicheSourceWidget.getMutableMappingTask().getMappingTask();
            IDataSourceProxy source = mappingTaskSource.getTargetProxy();
            source.getInstances();
            int returnVal = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //giannisk
                Scenarios.getNextFreeNumber();
                
                IDataSourceProxy target = loadXMLDataSource(chooser.getSelectedFile().getAbsolutePath());
                MappingTask mappingTask = new MappingTask(source, target, SpicyEngineConstants.LINES_BASED_MAPPING_TASK);
                caratteristicheWidgetChainComposition.getMutableMappingTask().setMappingTask(mappingTask);
                Scenario scenario = Utility.gestioneScenario(mappingTask, modello, actionProjectTree);
                chainWidget.changeImage(scenario.getImageNumber());
                chainWidget.revalidate();
            }
        } catch (DAOException ex) {
            logger.error(ex);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NEW_ERROR) + " : " + ex.getMessage(), DialogDescriptor.ERROR_MESSAGE));
        } catch (IllegalDataSourceException ex) {
            logger.error(ex);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.NO_INSTANCES_FOR_COMPOSITION)));

        }
    }

    private void deleteWidget() {
        LayerWidget mainLayer = (LayerWidget) chainWidget.getParentWidget();
        CaratteristicheWidgetChainComposition caratteristicheWidgetChainComposition = (CaratteristicheWidgetChainComposition) mainLayer.getChildConstraint(this.chainWidget);
//        ConnectionInfo connectionInfo = caratteristicheWidgetChainComposition.getConnectionInfo();
//        System.out.println("connectionInfo: " + connectionInfo);
//        if (connectionInfo != null) {
//            LayerWidget connectionLayer = (LayerWidget) connectionInfo.getConnectionWidget().getParentWidget();
            

            //TODO vedere se puo' essere migliorato mettendo una lista di connectionInfo nelle classe delle caratteristiche del widget (in questo caso in CaratteristicheWidgetChainComposition)
            List<ConnectionWidget> listaConnectionToWidget = new ArrayList<ConnectionWidget>();
            List<ConnectionWidget> listaConnectionFromWidget = new ArrayList<ConnectionWidget>();
            for (Widget widget : connectionLayer.getChildren()) {
                ConnectionWidget connectionWidget = (ConnectionWidget) widget;
                if (connectionWidget.getSourceAnchor().getRelatedWidget().equals(chainWidget)) {
                    listaConnectionFromWidget.add(connectionWidget);
                } else if (connectionWidget.getTargetAnchor().getRelatedWidget().equals(chainWidget)) {
                    listaConnectionToWidget.add(connectionWidget);
                }
            }
            for (ConnectionWidget connectionWidget : listaConnectionFromWidget) {
                manageTargetWidget(connectionWidget, caratteristicheWidgetChainComposition, mainLayer);
            }
            for (ConnectionWidget connectionWidget : listaConnectionToWidget) {
                manageSourceWidget(connectionWidget, caratteristicheWidgetChainComposition, mainLayer);
            }
//        }
        caratteristicheWidgetChainComposition.getWidgetBarra().removeFromParent();
        chainWidget.removeFromParent();
        chainWidget.getScene().validate();
    }

    private void executeInjection() {
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }


        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }


        if (this.actionProjectTree == null) {
            this.actionProjectTree = Lookups.forPath("Azione").lookup(ActionProjectTree.class);
        }
    }

    public void setEnable(boolean enabled) {
        this.itemLoadDataSource.setEnabled(enabled);
    }

    private void manageTargetWidget(ConnectionWidget connectionWidget, CaratteristicheWidgetChainComposition caratteristicheWidgetChainComposition, LayerWidget mainLayer) {

        AbstractCaratteristicheWidgetComposition caratteristicheWidgetCompositionTarget = (AbstractCaratteristicheWidgetComposition) mainLayer.getChildConstraint(connectionWidget.getTargetAnchor().getRelatedWidget());
        MutableMappingTask mutableMappingTaskSource = caratteristicheWidgetChainComposition.getMutableMappingTask();
        MutableMappingTask mutableMappingTaskTarget = caratteristicheWidgetCompositionTarget.getMutableMappingTask();
        mutableMappingTaskSource.getMutableMappingTasks().remove(mutableMappingTaskTarget);
        mutableMappingTaskTarget.getMutableMappingTasks().remove(mutableMappingTaskSource);

        caratteristicheWidgetCompositionTarget.removeSourceScenario((ICompositionWidget) connectionWidget.getTargetAnchor().getRelatedWidget());
        caratteristicheWidgetChainComposition.getTargetList().remove((ICompositionWidget) connectionWidget.getTargetAnchor().getRelatedWidget());

        connectionWidget.removeFromParent();

    }

    private void manageSourceWidget(ConnectionWidget connectionWidget, CaratteristicheWidgetChainComposition caratteristicheWidgetChainCompositionTarget, LayerWidget mainLayer) {

        AbstractCaratteristicheWidgetComposition caratteristicheWidgetCompositionSource = (AbstractCaratteristicheWidgetComposition) mainLayer.getChildConstraint(connectionWidget.getTargetAnchor().getRelatedWidget());
        MutableMappingTask mutableMappingTaskSource = caratteristicheWidgetCompositionSource.getMutableMappingTask();
        MutableMappingTask mutableMappingTaskTarget = caratteristicheWidgetChainCompositionTarget.getMutableMappingTask();
        mutableMappingTaskSource.getMutableMappingTasks().remove(mutableMappingTaskTarget);
        mutableMappingTaskTarget.getMutableMappingTasks().remove(mutableMappingTaskSource);

        caratteristicheWidgetChainCompositionTarget.removeSourceScenario((ICompositionWidget) connectionWidget.getTargetAnchor().getRelatedWidget());
        caratteristicheWidgetCompositionSource.getTargetList().remove((ICompositionWidget) connectionWidget.getTargetAnchor().getRelatedWidget());

        connectionWidget.removeFromParent();
    }
}
