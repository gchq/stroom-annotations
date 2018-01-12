import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { NavLink } from 'react-router-dom'
import RaisedButton from 'material-ui/RaisedButton'
import FlatButton from 'material-ui/FlatButton'
import Paper from 'material-ui/Paper';
import Toggle from 'material-ui/Toggle'
import { Toolbar, ToolbarGroup, ToolbarTitle } from 'material-ui/Toolbar'
import { fullWhite } from 'material-ui/styles/colors'
import Delete from 'material-ui-icons/Delete'
import Edit from 'material-ui-icons/Edit'
import Dialog from 'material-ui/Dialog'

import ReactTable from 'react-table'
import 'react-table/react-table.css'

import dateFormat from 'dateformat'

import SearchBar from 'material-ui-search-bar'

import SnackbarDisplay from '../snackbarDisplay'
import CreateAnnotation from '../createAnnotation'

import '../../styles/index.css'
import '../../styles/toolbar-small.css'
import '../../styles/toggle-small.css'

import './ManageAnnotations.css'
import '../../styles/table.css'

class ManageAnnotationsPage extends Component {
    state = {
        selectedId: undefined,
        removeDialogOpen: false
    };

    componentDidMount() {
        this.props.searchAnnotations(this.props.indexUuid);
    }

    handleOpen() {
        this.setState({ removeDialogOpen: true });
    };

    handleClose() {
        this.setState({ removeDialogOpen: false });
    };

    handleRemoveAndClose() {
        this.props.removeAnnotation(this.props.indexUuid, this.props.selectedAnnotationRowId)
        this.handleClose();
    }

    onSearchTermChange(e) {
        this.props.searchAnnotations(this.props.indexUuid, e);
    }

    editSelectedAnnotation() {
        this.props.history.push(`/singleWithNav/${this.props.indexUuid}/${this.props.selectedAnnotationRowId}`)
    };

    fetchMore() {
        this.props.moreAnnotations(this.props.indexUuid)
    }

    toggleRow(id) {
        // Tell the redux store so the control buttons get displayed correctly
        this.props.changeSelectedRow(id)
    }

    formatDate(dateString) {
        const dateFormatString = 'ddd mmm d yyyy, hh:MM:ss'
        return dateString ? dateFormat(dateString, dateFormatString) : ''
    }

    getColumnFormat() {
        return [{
            Header: '',
            accessor: 'id',
            Cell: row => (<div>{this.props.selectedAnnotationRowId === row.value ? 'selected' : 'unselected'}</div>),
            width: 30,
            filterable: false,
            show: false
        }, {
            Header: 'ID',
            accessor: 'id',
            width: 200
        }, {
            Header: 'Assigned To',
            accessor: 'assignTo',
            width: 200
        }, {
            Header: 'Status',
            accessor: 'status',
            width: 200
        }, {
            Header: 'Last Updated',
            accessor: 'lastUpdated',
            Cell: row => this.formatDate(row.value),
            width: 225,
            filterable: false
        }, {
            Header: 'Updated By',
            accessor: 'updatedBy',
            width: 200
        }, {
            Header: 'Content',
            accessor: 'content',
            width: 600
        }]
    }

    toggleFiltering(isFilteringEnabled) {
        this.setState({ isFilteringEnabled })
    }

    render() {
        const removeActions = [
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
        const deleteButtonDisabled = !this.props.selectedAnnotationRowId
        return (
            <Paper className='ManageAnnotations-main' zDepth={0}>
                <Toolbar className='toolbar-small'>
                    <ToolbarGroup>
                        <NavLink to={`/${this.props.indexUuid}`}>
                            <ToolbarTitle text='Annotations' className='toolbar-title-small' />
                        </NavLink>
                    </ToolbarGroup>
                    <ToolbarGroup>
                        <Toggle
                            className='toggle-small toggle-small-low'
                            label='Show filtering'
                            labelPosition='right'
                            onToggle={(event, isFilteringEnabled) => this.toggleFiltering(isFilteringEnabled)} />

                        <div>
                            <CreateAnnotation indexUuid={this.props.indexUuid} />
                        </div>
                        <div>
                            <RaisedButton label='View/Edit' primary
                                icon={<Edit color={fullWhite} />}
                                disabled={deleteButtonDisabled}
                                onClick={() => this.editSelectedAnnotation()}
                                className='toolbar-button-small' />
                        </div>
                        <div>
                            <RaisedButton label='Delete' primary
                                icon={<Delete color={fullWhite} />}
                                disabled={deleteButtonDisabled}
                                onClick={this.handleOpen.bind(this)}
                                className='toolbar-button-small' />
                        </div>
                    </ToolbarGroup>
                </Toolbar>
                <div className='manage-annotations__search-create-bar'>
                    <SearchBar value={this.props.searchTerm}
                        onChange={this.onSearchTermChange.bind(this)}
                        onRequestSearch={() => { }}
                    />
                </div>
                <div className='ManageAnnotations-content'>
                    <SnackbarDisplay />
                    <ReactTable
                        data={this.props.annotations}
                        className='-striped -highlight ManageAnnotations-table'
                        columns={this.getColumnFormat()}
                        filterable={this.state.isFilteringEnabled}
                        showPagination
                        loading={this.props.showSearchLoader}
                        getTheadTrProps={() => {
                            return {
                                className: 'table-header-small'
                            }
                        }}
                        getTheadProps={() => {
                            return {
                                className: 'table-row-small'
                            }
                        }}
                        getTrProps={(state, rowInfo) => {
                            let selected = false
                            if (rowInfo) {
                                selected = rowInfo.row.id === this.props.selectedAnnotationRowId
                            }
                            return {
                                onClick: (target, event) => {
                                    this.toggleRow(rowInfo.row.id)
                                },
                                className: selected ? 'table-row-small table-row-selected' : 'table-row-small'
                            }
                        }}
                    />
                </div>

                <Dialog
                    actions={removeActions}
                    modal={false}
                    open={this.state.removeDialogOpen}
                    onRequestClose={this.handleClose.bind(this)}
                >
                    Remove the Annotation {this.props.annotationId}?
                </Dialog>
            </Paper>
        )
    }
}

ManageAnnotationsPage.propTypes = {
    annotations: PropTypes.array.isRequired,
    searchTerm: PropTypes.string.isRequired,
    showSearchLoader: PropTypes.bool.isRequired,
    indexUuid: PropTypes.string.isRequired,
    canRequestMore: PropTypes.bool.isRequired,
    selectedAnnotationRowId: PropTypes.string,

    searchAnnotations: PropTypes.func.isRequired,
    moreAnnotations: PropTypes.func.isRequired,
    removeAnnotation: PropTypes.func.isRequired
}

export default ManageAnnotationsPage