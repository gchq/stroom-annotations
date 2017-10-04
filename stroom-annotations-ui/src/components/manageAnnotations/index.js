import React, { Component } from 'react'
import { connect } from 'react-redux'
import { withRouter } from 'react-router'
import moment from 'moment'

import AppBar from 'material-ui/AppBar'

import Paper from 'material-ui/Paper';

import PendingUpdatesSpinner from '../pendingUpdatesSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'
import CreateAnnotation from '../createAnnotation'
import SearchBar from 'material-ui-search-bar'

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
            </TableRow>
        ))
    }


    handleRowSelection = (selectedRows) => {
        let annotation = this.props.annotations[selectedRows]
        this.props.history.push(`/singleEdit/${annotation.id}`)
    };

    render (props) {
        return (
            <div className='app'>
                <AppBar
                    title='Manage Annotations'
                    iconElementLeft={<div/>}
                    iconElementRight={<ErrorDisplay />}
                    />
                <PendingUpdatesSpinner />
                <SnackbarDisplay />
                <Paper className='app--body' zDepth={0}>
                    <div className='manage-annotations__search-create-bar'>
                        <SearchBar value={this.props.searchTerm}
                                    onChange={this.onSearchTermChange.bind(this)}
                                    onRequestSearch={() => {}}
                                    />
                        <CreateAnnotation />
                    </div>

                    <Table  selectable={true}
                            onRowSelection={this.handleRowSelection.bind(this)}>
                        <TableHeader
                                adjustForCheckbox={false}
                                displaySelectAll={false}
                                enableSelectAll={false}>
                            <TableRow>
                                <TableHeaderColumn>ID</TableHeaderColumn>
                                <TableHeaderColumn>Status</TableHeaderColumn>
                                <TableHeaderColumn>Assign To</TableHeaderColumn>
                                <TableHeaderColumn>Content</TableHeaderColumn>
                                <TableHeaderColumn>Last Update</TableHeaderColumn>
                            </TableRow>
                        </TableHeader>
                        <TableBody
                                displayRowCheckbox={false}
                                showRowHover={true}
                            >
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
        annotations: state.manageAnnotations.annotations,
        searchTerm: state.manageAnnotations.searchTerm
    }),
    {
        searchAnnotations
    }
)(withRouter(ManageAnnotations));