import React, { Component } from 'react';
import PropTypes from 'prop-types'

import { NavLink } from 'react-router-dom'

import RaisedButton from 'material-ui/RaisedButton'
import Subheader from 'material-ui/Subheader'
import Paper from 'material-ui/Paper'
import KeyboardArrowRight from 'material-ui-icons/KeyboardArrowRight'
import { Toolbar, ToolbarGroup, ToolbarTitle } from 'material-ui/Toolbar'

import EditAnnotation from '../editAnnotation';
import SnackbarDisplay from '../snackbarDisplay'

import './singleAnnotation.css'

class SingleAnnotationPage extends Component {
    componentDidMount() {
        this.props.fetchAnnotation(this.props.indexUuid, this.props.annotationId)
    }

    handleViewHistory() {
        if (this.props.allowNavigation) {
            this.props.history.push(`/historyWithNav/${this.props.indexUuid}/${this.props.annotationId}`)
        } else {
            this.props.history.push(`/history/${this.props.indexUuid}/${this.props.annotationId}`)
        }
    }

    render() {
        let annotationComponent = undefined;

        // Decide on the annotation component
        if (this.props.annotation.id) {
            annotationComponent = <EditAnnotation
                indexUuid={this.props.indexUuid}
                annotationId={this.props.annotationId}
                allowNavigation={this.props.allowNavigation}
            />
        } else if (!this.props.isClean) {
            annotationComponent = <Subheader>Waiting...</Subheader>
        } else {
            annotationComponent = (
                <div>
                    <RaisedButton
                        label="Create Annotation"
                        primary={true}
                        onClick={() => this.props.createAnnotation(this.props.indexUuid, this.props.annotationId)}
                        className='single-annotation__create-button'
                    />

                    <RaisedButton
                        label="View History"
                        primary={true}
                        onClick={this.handleViewHistory.bind(this)}
                        className='single-annotation__history-button'
                    />
                </div>
            )
        }

        let rootNav = (this.props.allowNavigation) ?
            <NavLink to={`/${this.props.indexUuid}`}>
                <ToolbarTitle text='Annotations' className='toolbar-title-small' />
            </NavLink>
            :
            <ToolbarTitle text='Annotations' className='toolbar-title-small' />

        return (
            <Paper className='ManageAnnotations-main' zDepth={0}>
                <Toolbar className='toolbar-small'>
                    <ToolbarGroup>
                        {rootNav}
                        <KeyboardArrowRight />
                        <ToolbarTitle text='Edit Annotation' className='toolbar-title-small' />
                    </ToolbarGroup>
                </Toolbar>

                <SnackbarDisplay />

                {annotationComponent}
            </Paper>
        );
    }
}

SingleAnnotationPage.propTypes = {
    // set from routing
    indexUuid: PropTypes.string.isRequired,
    annotationId: PropTypes.string.isRequired,
    allowNavigation: PropTypes.bool.isRequired,

    // set by react router
    history: PropTypes.object.isRequired,

    // Conencted to redux state
    annotation: PropTypes.object.isRequired,

    // Connected to redux actions
    createAnnotation: PropTypes.func.isRequired,
    fetchAnnotation: PropTypes.func.isRequired
}

export default SingleAnnotationPage