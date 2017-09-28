import React, { Component } from 'react'
import { connect } from 'react-redux'
import {NavLink} from 'react-router-dom'
import moment from 'moment'

import AppBar from 'material-ui/AppBar'

import IconButton from 'material-ui/IconButton'
import EditIcon from 'material-ui/svg-icons/editor/mode-edit'
import DeleteIcon from 'material-ui/svg-icons/action/delete'
import FlatButton from 'material-ui/FlatButton'
import Dialog from 'material-ui/Dialog'
import Paper from 'material-ui/Paper';

import PendingUpdatesSpinner from '../pendingUpdatesSpinner'
import CreateAnnotation from '../createAnnotation'
import SearchBar from 'material-ui-search-bar'

import { removeAnnotation } from '../../actions/removeAnnotation'
import { searchAnnotations } from '../../actions/searchAnnotations'

import {
    Table,
    TableBody,
    TableHeader,
    TableHeaderColumn,
    TableRow,
    TableRowColumn,
} from 'material-ui/Table';

import '../appStyle/app.css'
import '../appStyle/dialog.css'
import './manageAnnotations.css'

class ManageAnnotations extends Component {
    state = {
        selectedId: undefined,
        removeDialogOpen: false
    };

    componentDidMount() {
        this.props.searchAnnotations();
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

    onSearchTermChange(e) {
        this.props.searchAnnotations(e);
    }

    renderRows() {
        return this.props.annotations.map((a, i) => (
            <TableRow key={a.id}>
                <TableRowColumn>{a.id}</TableRowColumn>
                <TableRowColumn>{a.status}</TableRowColumn>
                <TableRowColumn>{a.assignTo}</TableRowColumn>
                <TableRowColumn>{a.content}</TableRowColumn>
                <TableRowColumn>{
                    `${moment(a.lastUpdated).fromNow()} by ${a.updatedBy}`
                }</TableRowColumn>
                <TableRowColumn>
                    <NavLink to={`/singleEdit/${a.id}`}>
                        <IconButton tooltip="edit">
                            <EditIcon />
                        </IconButton>
                    </NavLink>
                    <IconButton tooltip="delete" onClick={() => this.handleRemoveDialogOpen(a.id)}>
                        <DeleteIcon />
                    </IconButton>
                </TableRowColumn>
            </TableRow>
        ))
    }

    render (props) {
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

        return (
            <div className='app'>
                <AppBar
                    title='Manage Annotations'
                    iconElementLeft={<div/>}
                    iconElementRight={<PendingUpdatesSpinner />}
                    />
                <Paper className='app--body' zDepth={0}>
                    <div className='manage-annotations__search-create-bar'>
                        <SearchBar value={this.props.searchTerm}
                                    onChange={this.onSearchTermChange.bind(this)}
                                    onRequestSearch={() => {}}
                                    />
                        <CreateAnnotation />
                    </div>

                    <Dialog
                        actions={removeDialogActions}
                        modal={false}
                        open={this.state.removeDialogOpen}
                        onRequestClose={this.handleRemoveDialogClose.bind(this)}
                        contentClassName='dialog'
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
                            <TableHeaderColumn>Last Update</TableHeaderColumn>
                            <TableHeaderColumn>Actions</TableHeaderColumn>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {this.renderRows()}
                        </TableBody>
                    </Table>
                </Paper>
            </div>
        )
    }
}

export default ManageAnnotations = connect(
    (state) => ({
        annotations: state.annotations.annotations,
        searchTerm: state.annotations.searchTerm
    }),
    {
        removeAnnotation,
        searchAnnotations
    }
)(ManageAnnotations);