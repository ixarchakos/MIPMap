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
 
package it.unibas.spicygui.controllo.provider.intermediatezone;

import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.widget.caratteristiche.ConnectionInfo;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterFunction;
import it.unibas.spicygui.widget.operators.ConnectionInfoCreator;
import it.unibas.spicygui.controllo.mapping.operators.CreateCorrespondencesMappingTask;
import it.unibas.spicygui.controllo.mapping.operators.ReviewCorrespondences;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetTree;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.beansbinding.BindingGroup;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

public class MyPopupProviderConnectionFunc implements PopupMenuProvider, ActionListener {

    private static Log logger = LogFactory.getLog(MyPopupProviderConnectionFunc.class);
    private ReviewCorrespondences review = new ReviewCorrespondences();
    private CreateCorrespondencesMappingTask creator = new CreateCorrespondencesMappingTask();
    private ConnectionInfoCreator infoCreator = new ConnectionInfoCreator();
    private CaratteristicheWidgetInterFunction caratteristicheWidget;
    private JPopupMenu menu;
//    private JMenuItem itemConfidence;
//    private JCheckBoxMenuItem itemImplied;
    private BindingGroup bindingGroup;
    private boolean first = true;
    private ConnectionWidget connection;
    private Scene scene;
    private LayerWidget mainLayer;
    private final String DELETE = "delete";

    public MyPopupProviderConnectionFunc(Scene scene, LayerWidget mainLayer, CaratteristicheWidgetInterFunction caratteristicheWidget) {
        this.caratteristicheWidget = caratteristicheWidget;
        this.scene = scene;
        this.mainLayer = mainLayer;
        createPopupMenu();
    }

    public JPopupMenu getPopupMenu(Widget widget, Point arg1) {
        if (first) {
            connection = (ConnectionWidget) widget;
           // createInfoLabel();
            first = false;
        }
        return menu;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(DELETE)) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.DELETE_CONNECTION));
            deleteConnection();
        } else {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.GENERIC_ERROR));
        }
    }

    private void createPopupMenu() {
        menu = new JPopupMenu("Popup menu");
        JMenuItem item;

//        item = new JMenuItem(NbBundle.getMessage(Costanti.class, Costanti.SHOW_HIDE_INFO_CONNECTION));
//        item.setActionCommand(SHOW);
//        item.addActionListener(this);
//        menu.add(item);
//

//
//        itemConfidence = new JMenuItem();
//        menu.add(itemConfidence);
//
//        itemImplied = new JCheckBoxMenuItem(NbBundle.getMessage(Costanti.class, Costanti.IMPLIED));
//        menu.add(itemImplied);

        item = new JMenuItem(NbBundle.getMessage(Costanti.class, Costanti.DELETE_CONNECTION));
        item.setActionCommand(DELETE);
        item.addActionListener(this);
        menu.add(item);

    }

    private void createInfoLabel() {
        ConnectionInfo connectionInfo = (ConnectionInfo) connection.getParentWidget().getChildConstraint(connection);
        bindingGroup = new BindingGroup();
//        VMDNodeWidget vmdNodeWidget = infoCreator.createPropertyWidget(scene, bindingGroup, connectionInfo, connection);
////        infoCreator.createPropertyItem(itemConfidence, itemImplied, bindingGroup, connectionInfo, connection);
        bindingGroup.bind();
//        connection.setConstraint(vmdNodeWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER, 0.5f);
        scene.validate();
//        vmdNodeWidget.getActions().addAction(ActionFactory.createPopupMenuAction(new MyPopupProviderConnectionInfo(scene)));
//        vmdNodeWidget.getActions().addAction(ActionFactory.createSelectAction(new MySelectActionProvider()));
    }
    
    private void deleteConnection() {
        if (caratteristicheWidget.getSourceList().remove(connection.getSourceAnchor().getRelatedWidget())) {
            review.removeCorrespondence(caratteristicheWidget.getValueCorrespondence());
            if (logger.isDebugEnabled()) {
                logger.debug("rimuovo source - target: " + caratteristicheWidget.getTargetWidget());
            }
            if (caratteristicheWidget.getTargetWidget() != null && !caratteristicheWidget.getSourceList().isEmpty() ) {
                creator.createCorrespondenceWithFunction(mainLayer, caratteristicheWidget.getTargetWidget(), caratteristicheWidget, caratteristicheWidget.getConnectionInfo());
            } else if (caratteristicheWidget.getSourceList().isEmpty()) {
                //TODO vedere se sia opportuno elimanare la connessione verso il target nel momento non ci siano connessioni dal source
            }            
            removeConnectionAnnotations(connection.getSourceAnchor().getRelatedWidget(), connection.getTargetAnchor().getRelatedWidget(), connection, true);
            connection.removeFromParent(); 
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("rimuovo target");
            }
            review.removeCorrespondence(caratteristicheWidget.getValueCorrespondence());
            caratteristicheWidget.setTargetWidget(null);           
            removeConnectionAnnotations(connection.getSourceAnchor().getRelatedWidget(), connection.getTargetAnchor().getRelatedWidget(), connection, false);
            connection.removeFromParent();
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Costanti.class, Costanti.DELETE_CONNECTION));
    }
    
    //giannisk
    private void removeConnectionAnnotations(Widget sourceWidget, Widget targetWidget, ConnectionWidget connection, boolean target){
        CaratteristicheWidgetTree caratteristicheWidgetTreeSource;
        if (target){
            caratteristicheWidgetTreeSource = (CaratteristicheWidgetTree) mainLayer.getChildConstraint(sourceWidget);
        }
        else{
            caratteristicheWidgetTreeSource = (CaratteristicheWidgetTree) mainLayer.getChildConstraint(targetWidget);
        }
        INode iNode = caratteristicheWidgetTreeSource.getINode();
        List<ConnectionWidget> connections = (List<ConnectionWidget>) iNode.getAnnotation(Costanti.CONNECTION_LINE);
        connections.remove(connection);
        Iterator<ConnectionWidget> iterator = connections.iterator();
        while (iterator.hasNext()){
            ConnectionWidget conn = iterator.next();
            ConnectionInfo connectionInfo = (ConnectionInfo) connection.getParentWidget().getChildConstraint(conn);
            if (connectionInfo!=null){
                if (target){
                    if(connectionInfo.getSourceWidget()!=null)
                        if (connectionInfo.getSourceWidget().equals(targetWidget))
                            iterator.remove();
                }
                else{
                   if(connectionInfo.getTargetWidget()!=null)
                        if (connectionInfo.getTargetWidget().equals(sourceWidget))
                            iterator.remove();
                }
            }
        }
        iNode.addAnnotation(Costanti.CONNECTION_LINE, connections);
    }

}
