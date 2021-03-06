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
 
package it.unibas.spicygui.controllo.provider;

import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.datasource.JoinCondition;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.commons.Modello;
import it.unibas.spicygui.controllo.provider.intermediatezone.MyPopupProviderConnectionFunc;
import it.unibas.spicygui.widget.caratteristiche.ConnectionInfo;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterFunction;
import it.unibas.spicygui.widget.FunctionWidget;
import it.unibas.spicygui.widget.VMDPinWidgetSource;
import it.unibas.spicygui.widget.VMDPinWidgetTarget;
import it.unibas.spicygui.controllo.mapping.operators.ICreateCorrespondences;
import it.unibas.spicygui.controllo.mapping.operators.ReviewCorrespondences;
import it.unibas.spicygui.controllo.provider.intermediatezone.MyPopupProviderConnectionFunctionalDep;
import it.unibas.spicygui.vista.Vista;
import it.unibas.spicygui.widget.FunctionalDependencyWidget;
import it.unibas.spicygui.widget.JoinConstraint;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetInterFunctionalDep;
import it.unibas.spicygui.widget.caratteristiche.CaratteristicheWidgetTree;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;

public class ActionSceneConnection implements ConnectProvider {

    private Scene scene;
    private LayerWidget connectionLayer;
    private LayerWidget mainLayer;
    private Modello modello;
    private Vista vista;
    private ICreateCorrespondences creator;
    private ReviewCorrespondences review = new ReviewCorrespondences();
    private static Log logger = LogFactory.getLog(ActionSceneConnection.class);

    public ActionSceneConnection(Scene scene, LayerWidget connectionLayer, LayerWidget mainLayer, ICreateCorrespondences creator) {
        this.scene = scene;
        this.connectionLayer = connectionLayer;
        this.mainLayer = mainLayer;
        this.creator = creator;

    }

    public boolean isSourceWidget(Widget source) {
        return true;
    }

    public ConnectorState isTargetWidget(Widget source, Widget target) {
        //TODO implementare acnhe la condizione verso una funzione
        if ((source instanceof VMDPinWidgetSource && target instanceof VMDPinWidgetTarget) && (source != target) && (target.isEnabled())) {
            return ConnectorState.ACCEPT;
        }
        if ((source instanceof VMDPinWidgetSource && target instanceof FunctionWidget) && (source != target)) {
            return ConnectorState.ACCEPT;
        }
        if ((source instanceof VMDPinWidgetSource && target instanceof VMDPinWidgetSource) && (source != target) && (target.isEnabled())) {
            return ConnectorState.ACCEPT;
        }
        if ((source instanceof VMDPinWidgetSource && target instanceof FunctionalDependencyWidget) && (source != target) && (target.isEnabled())) {
            CaratteristicheWidgetInterFunctionalDep caratteristiche = (CaratteristicheWidgetInterFunctionalDep) target.getParentWidget().getChildConstraint(target);
            if (caratteristiche.isSource() == null || caratteristiche.isSource()) {
                 caratteristiche.setSource(Boolean.TRUE);
                return ConnectorState.ACCEPT;
            }
        }
        return ConnectorState.REJECT_AND_STOP;
    }

    public boolean hasCustomTargetWidgetResolver(Scene arg0) {
        return false;
    }

    public Widget resolveTargetWidget(Scene arg0, Point arg1) {
        return null;
    }

    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        ConnectionWidget connection = new ConnectionWidget(scene);
        connection.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        connection.setSourceAnchor(AnchorFactory.createCenterAnchor(sourceWidget));
        connection.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget));
        Stroke stroke = creator.getStroke();
        connection.setStroke(stroke); 
        if (targetWidget instanceof FunctionWidget) {
            setWidget(sourceWidget, targetWidget, connection);
            addConnectionAnnotation(sourceWidget, connection);
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.setSourceWidget(sourceWidget);
            connectionInfo.setTargetWidget(targetWidget);
            connectionInfo.setConnectionWidget(connection);
            connectionLayer.addChild(connection, connectionInfo);
        }
        if (targetWidget instanceof FunctionalDependencyWidget) {
            setWidgetFunctionalDependency(sourceWidget, targetWidget, connection);
            addConnectionAnnotation(sourceWidget, connection);
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.setSourceWidget(sourceWidget);
            connectionInfo.setTargetWidget(targetWidget);
            connectionInfo.setConnectionWidget(connection);
            connectionLayer.addChild(connection, connectionInfo);
        }
        if (targetWidget instanceof VMDPinWidgetTarget) {
            //giannisk
            //add annotation to node, so that when it is selected its correspondences are highlighted with red color
            addConnectionAnnotation(sourceWidget, connection);
            addConnectionAnnotation(targetWidget, connection);
            createCorrespondence(connection, sourceWidget, targetWidget);
        }
        if (targetWidget instanceof VMDPinWidgetSource) {
            if (createJoinCondition(sourceWidget, targetWidget)) {
                return;
            }
        }

    }
    
    //giannisk
    public void addConnectionAnnotation(Widget widget, ConnectionWidget connection){
        CaratteristicheWidgetTree caratteristicheWidgetTreeSource = (CaratteristicheWidgetTree) mainLayer.getChildConstraint(widget);
        INode iNode = caratteristicheWidgetTreeSource.getINode();
        List<ConnectionWidget> connections = (List<ConnectionWidget>) iNode.getAnnotation(Costanti.CONNECTION_LINE);
        if (connections == null){
            connections = new ArrayList<ConnectionWidget>();
        }
        connections.add(connection);
        iNode.addAnnotation(Costanti.CONNECTION_LINE, connections);
    }

    // <editor-fold defaultstate="collapsed" desc="metodi di creazione connessioni">
    private void createCorrespondence(ConnectionWidget connection, Widget sourceWidget, Widget targetWidget) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setConnectionWidget(connection);
        connectionLayer.addChild(connection, connectionInfo);
        connection.getActions().addAction(ActionFactory.createPopupMenuAction(new MyPopupProviderConnectionMappingTask(scene)));
        connection.getActions().addAction(ActionFactory.createSelectAction(new MySelectConnectionActionProvider(connectionLayer)));
        creator.createCorrespondence(mainLayer, sourceWidget, targetWidget, connectionInfo);
        if (connectionInfo.getValueCorrespondence() != null) {
            connection.setToolTipText(connectionInfo.getValueCorrespondence().toString());
        } else {
            connection.setToolTipText(connectionInfo.getVariableCorrespondence().toString());
        }
    }

    private boolean createJoinCondition(Widget sourceWidget, Widget targetWidget) {
        executeInjection();
        if (modello.getBean(Costanti.CREATING_JOIN_SESSION) == null || modello.getBean(Costanti.JOIN_SESSION_TARGET) != null) {
//TODO il codice commentato si riferisce alla possibilità di creare join multipli bisgna vedere dove farlo, invece che in questo punto
//                String[] options = new String[]{NbBundle.getMessage(Costanti.class, Costanti.JOIN_DIALOG_SINGLE), NbBundle.getMessage(Costanti.class, Costanti.JOIN_DIALOG_MULTIPLE), NbBundle.getMessage(Costanti.class, Costanti.JOIN_DIALOG_CANCEL)};
//                int scelta = JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(Costanti.class, Costanti.JOIN_DIALOG_MESSAGE), NbBundle.getMessage(Costanti.class, Costanti.JOIN_DIALOG_TITLE), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
//                if (scelta == 2 || scelta == JOptionPane.CLOSED_OPTION) {
//                    return;
//                }
//                if (scelta == 0) {
            creator.createCorrespondenceJoinCondition(mainLayer, sourceWidget, targetWidget, true);
            return true;
        } else {
            if (this.modello.getBean(Costanti.JOIN_CONSTRIANTS) == null || this.modello.getBean(Costanti.JOIN_CONDITION) == null || this.modello.getBean(Costanti.FROM_PATH_NODES) == null) {
                JoinConstraint joinConstraint = new JoinConstraint();
                List<INode> fromPathNodes = new ArrayList<INode>();
                JoinCondition joinCondition = creator.createCorrespondenceJoinCondition(mainLayer, sourceWidget, targetWidget, joinConstraint, null, fromPathNodes, true);
                this.modello.putBean(Costanti.CREATING_JOIN_SESSION, new Boolean(true));
                this.modello.putBean(Costanti.JOIN_SESSION_SOURCE, new Boolean(true));
                this.modello.putBean(Costanti.JOIN_CONSTRIANTS, joinConstraint);
                this.modello.putBean(Costanti.JOIN_CONDITION, joinCondition);
                this.modello.putBean(Costanti.FROM_PATH_NODES, fromPathNodes);
                //caso di join multiplo inizio
                vista.getMultipleJDialog().setVisible(true);
                vista.getMultipleJDialog().setText(joinCondition.toString());
            } else {
                JoinConstraint joinConstraint = (JoinConstraint) this.modello.getBean(Costanti.JOIN_CONSTRIANTS);
                JoinCondition joinCondition = (JoinCondition) this.modello.getBean(Costanti.JOIN_CONDITION);
                List<INode> fromPathNodes = (List<INode>) this.modello.getBean(Costanti.FROM_PATH_NODES);
                //caso di join multiplo già in corso
                creator.createCorrespondenceJoinCondition(mainLayer, sourceWidget, targetWidget, joinConstraint, joinCondition, fromPathNodes, true);
                vista.getMultipleJDialog().setText(joinCondition.toString());
                //TODO : Gestire il caso in cui siamo in  sessione di join
            }
        }
        return false;
    }
// </editor-fold>

    private void setWidget(Widget sourceWidget, Widget targetWidget, ConnectionWidget connection) {
        CaratteristicheWidgetInterFunction caratteristiche = (CaratteristicheWidgetInterFunction) targetWidget.getParentWidget().getChildConstraint(targetWidget);
        connection.getActions().addAction(ActionFactory.createPopupMenuAction(new MyPopupProviderConnectionFunc(sourceWidget.getScene(), mainLayer, caratteristiche)));
        caratteristiche.addSourceWidget((VMDPinWidgetSource) sourceWidget);
        if (caratteristiche.getTargetWidget() != null) {
            review.removeCorrespondence(caratteristiche.getValueCorrespondence());
            creator.createCorrespondenceWithFunction(mainLayer, caratteristiche.getTargetWidget(), caratteristiche, caratteristiche.getConnectionInfo());
        }
    }

    private void setWidgetFunctionalDependency(Widget sourceWidget, Widget targetWidget, ConnectionWidget connection) {
        CaratteristicheWidgetInterFunctionalDep caratteristiche = (CaratteristicheWidgetInterFunctionalDep) targetWidget.getParentWidget().getChildConstraint(targetWidget);
        connection.getActions().addAction(ActionFactory.createPopupMenuAction(new MyPopupProviderConnectionFunctionalDep(sourceWidget.getScene(), mainLayer, caratteristiche)));
        caratteristiche.addSourceWidget((VMDPinWidget) sourceWidget);
        if (!caratteristiche.getTargetList().isEmpty()) {
            review.removeFunctionalDependency(caratteristiche.getFunctionalDependency(), caratteristiche.isSource());
            creator.createCorrespondenceWithFunctionalDep(mainLayer, caratteristiche, caratteristiche.getConnectionInfo());
        }
    }

    private void executeInjection() {
        if (this.modello == null) {
            this.modello = Lookup.getDefault().lookup(Modello.class);
        }
        if (this.vista == null) {
            this.vista = Lookup.getDefault().lookup(Vista.class);
        }
    }
}
