import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'

import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper';
import SearchBar from 'material-ui-search-bar'
import IconButton from 'material-ui/IconButton'
import MoreHorizIcon from 'material-ui/svg-icons/navigation/more-horiz'

import ApiCallSpinner from '../apiCallSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'
import CreateAnnotation from '../createAnnotation'

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
import './manageAnnotationsPage.css'

class ManageAnnotationsPage extends Component {
    state = {
        selectedId: undefined,
        removeDialogOpen: false
    };

    componentDidMount() {
        this.props.searchAnnotations(this.props.indexUuid);
    }

    onSearchTermChange(e) {
        this.props.searchAnnotations(this.props.indexUuid, e);
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


    handleRowSelection(selectedRows) {
        let annotation = this.props.annotations[selectedRows]
        this.props.history.push(`/singleWithNav/${this.props.indexUuid}/${annotation.id}`)
    };

    fetchMore() {
        this.props.moreAnnotations(this.props.indexUuid)
    }

    render () {
        const styles = {
            moreDiv: {
                display: 'flex',
                justifyContent: 'space-around'
            },
            largeIcon: {
                width: 60,
                height: 60,
            },
            large: {
                width: 120,
                height: 120,
                padding: 30,
            },
        };

        let requestMoreWidget = undefined
        if (this.props.canRequestMore) {
            requestMoreWidget = (
                <div style={styles.moreDiv}>
                    <IconButton style={styles.large}
                                iconStyle={styles.largeIcon}
                                onClick={this.fetchMore.bind(this)}>
                        <MoreHorizIcon />
                    </IconButton>
                </div>
            )
        }

        return (
            <div className='app'>
                <AppBar
                    title='Manage Annotations'
                    iconElementLeft={<div/>}
                    iconElementRight={<ErrorDisplay />}
                    />
                <ApiCallSpinner />
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
                    {requestMoreWidget}
                </Paper>
            </div>
        )
    }
}

ManageAnnotationsPage.propTypes = {
    annotations: PropTypes.array.isRequired,
    searchTerm: PropTypes.string.isRequired,
    indexUuid: PropTypes.string.isRequired,
    canRequestMore: PropTypes.bool.isRequired,

    searchAnnotations: PropTypes.func.isRequired,
    moreAnnotations: PropTypes.func.isRequired
}

export default ManageAnnotationsPage