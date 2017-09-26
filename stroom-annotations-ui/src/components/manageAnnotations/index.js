import React, { Component } from 'react';
import { connect } from 'react-redux'
import {NavLink} from 'react-router-dom'

import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper'

import IconButton from 'material-ui/IconButton';
import EditIcon from 'material-ui/svg-icons/editor/mode-edit';
import DeleteIcon from 'material-ui/svg-icons/action/delete';
import FlatButton from 'material-ui/FlatButton';

import CleanIndicator from '../cleanIndicator'
import SearchAnnotationBox from '../searchAnnotationBox'
import Dialog from 'material-ui/Dialog';

import { removeAnnotation } from '../../actions/removeAnnotation';

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
        open: false,
    };

    handleOpen(selectedId) {
        this.setState({
            open: true,
            selectedId
        });
    };

    handleClose() {
        this.setState({open: false});
    };

    handleRemoveAndClose() {
        this.props.removeAnnotation(this.state.selectedId)
        this.handleClose();
    }

    render (props) {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose.bind(this)}
                />,
            <FlatButton
                label="Remove"
                primary={true}
                onClick={this.handleRemoveAndClose.bind(this)}
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
                    iconElementRight={<CleanIndicator />}
                    />
                <Paper className='app--body'>
                    <SearchAnnotationBox />
                    <p>Annotations will go here</p>

                    <Dialog
                      actions={actions}
                      modal={false}
                      open={this.state.open}
                      onRequestClose={this.handleClose.bind(this)}
                      contentStyle={customContentStyle}
                    >
                        Remove the Annotation for event {this.props.selectedId}?
                    </Dialog>

                    <Table selectable={false}>
                        <TableHeader>
                            <TableRow>
                            <TableHeaderColumn>ID</TableHeaderColumn>
                            <TableHeaderColumn>Status</TableHeaderColumn>
                            <TableHeaderColumn>Content</TableHeaderColumn>
                            <TableHeaderColumn>Actions</TableHeaderColumn>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {this.props.annotations.map((a, i) => (
                                <TableRow key={a.id}>
                                    <TableRowColumn>{a.id}</TableRowColumn>
                                    <TableRowColumn>{a.status}</TableRowColumn>
                                    <TableRowColumn>{a.content}</TableRowColumn>
                                    <TableRowColumn>
                                        <NavLink to={`/single/edit/${a.id}`}>
                                            <IconButton tooltip="edit">
                                                <EditIcon />
                                            </IconButton>
                                        </NavLink>
                                        <IconButton tooltip="delete" onClick={() => this.handleOpen(a.id)}>
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
     removeAnnotation
  }
)(ManageAnnotations);