import React, { Component } from 'react'
import { connect } from 'react-redux'
import {NavLink} from 'react-router-dom'

import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper'

import IconButton from 'material-ui/IconButton'
import EditIcon from 'material-ui/svg-icons/editor/mode-edit'
import DeleteIcon from 'material-ui/svg-icons/action/delete'
import RaisedButton from 'material-ui/RaisedButton'
import FlatButton from 'material-ui/FlatButton'
import Dialog from 'material-ui/Dialog'
import TextField from 'material-ui/TextField'

import PendingUpdatesSpinner from '../pendingUpdatesSpinner'
import SearchAnnotationBox from '../searchAnnotationBox'

import { removeAnnotation } from '../../actions/removeAnnotation'
import { createAnnotation } from '../../actions/createAnnotation'

import {
    Table,
    TableBody,
    TableHeader,
    TableHeaderColumn,
    TableRow,
    TableRowColumn,
} from 'material-ui/Table';

import './../app/App.css'

class ManageAnnotations extends Component {
    state = {
        selectedId: undefined,
        removeDialogOpen: false,
        createDialogOpen: false,
        newAnnotationId: undefined
    };

    onNewAnnotationIdChange(e) {
        this.setState({
            newAnnotationId: e.target.value
        })
    }

    // Create dialog
    handleCreateDialogOpen() {
        this.setState({
            createDialogOpen: true
        });
    };

    handleCreateDialogClose() {
        this.setState({
            createDialogOpen: false,
            newAnnotationId: undefined
        });
    };

    handleConfirmCreate() {
        this.props.createAnnotation(this.state.newAnnotationId)
        this.handleCreateDialogClose()
    }

    // Remove Dialog
    handleRemoveDialogOpen(selectedId) {
        this.setState({
            removeDialogOpen: true,
            selectedId
        });
    };

    handleRemoveDialogClose() {
        this.setState({removeDialogOpen: false});
    };

    handleConfirmRemove() {
        this.props.removeAnnotation(this.state.selectedId)
        this.handleRemoveDialogClose();
    }

    render (props) {
        const createDialogActions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleCreateDialogClose.bind(this)}
                />,
            <FlatButton
                label="Create"
                primary={true}
                onClick={this.handleConfirmCreate.bind(this)}
                />,
        ];
    
        const removeDialogActions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleRemoveDialogClose.bind(this)}
                />,
            <FlatButton
                label="Remove"
                primary={true}
                onClick={this.handleConfirmRemove.bind(this)}
                />,
        ];

        const customContentStyle = {
            width: '20rem',
            maxWidth: 'none',
        };

        return (
            <div className='app'>
                <AppBar
                    title='Manage Annotations'
                    iconElementLeft={<div/>}
                    iconElementRight={<PendingUpdatesSpinner />}
                    />
                <Paper className='app--body'>
                    <SearchAnnotationBox />
                    
                    <Dialog
                          actions={createDialogActions}
                          modal={false}
                          open={this.state.createDialogOpen}
                          onRequestClose={this.handleCreateDialogClose.bind(this)}
                          contentStyle={customContentStyle}
                    >
                         <TextField value={this.state.newAnnotationId} onChange={this.onNewAnnotationIdChange.bind(this)}
                                                hintText="Enter the ID for the new annotation"
                                                floatingLabelText="Annotation ID"
                                                fullWidth={true}
                                            />
                    </Dialog>
                    
                    <RaisedButton
                        label="Create"
                        primary={true}
                        onClick={this.handleCreateDialogOpen.bind(this)}
                        />
                
                    <Dialog
                        actions={removeDialogActions}
                        modal={false}
                        open={this.state.removeDialogOpen}
                        onRequestClose={this.handleRemoveDialogClose.bind(this)}
                        contentStyle={customContentStyle}
                    >
                        Remove the Annotation for {this.state.selectedId}?
                    </Dialog>

                    <Table selectable={false}>
                        <TableHeader>
                            <TableRow>
                            <TableHeaderColumn>ID</TableHeaderColumn>
                            <TableHeaderColumn>Status</TableHeaderColumn>
                            <TableHeaderColumn>Assign To</TableHeaderColumn>
                            <TableHeaderColumn>Content</TableHeaderColumn>
                            <TableHeaderColumn>Actions</TableHeaderColumn>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {this.props.annotations.map((a, i) => (
                                <TableRow key={a.id}>
                                    <TableRowColumn>{a.id}</TableRowColumn>
                                    <TableRowColumn>{a.status}</TableRowColumn>
                                    <TableRowColumn>{a.assignTo}</TableRowColumn>
                                    <TableRowColumn>{a.content}</TableRowColumn>
                                    <TableRowColumn>
                                        <NavLink to={`/single/edit/${a.id}`}>
                                            <IconButton tooltip="edit">
                                                <EditIcon />
                                            </IconButton>
                                        </NavLink>
                                        <IconButton tooltip="delete" onClick={() => this.handleRemoveDialogOpen(a.id)}>
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableRowColumn>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
            </div>
        )
    }
}

export default ManageAnnotations = connect(
    (state) => ({
        annotations: state.annotations.annotations
    }),
    {
        createAnnotation,
        removeAnnotation
    }
)(ManageAnnotations);