import React, { Component } from 'react';
import PropTypes from 'prop-types'

import EditAnnotation from '../editAnnotation';

import ApiCallSpinner from '../apiCallSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'

import IconButton from 'material-ui/IconButton'
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back'
import AppBar from 'material-ui/AppBar'
import RaisedButton from 'material-ui/RaisedButton'
import Subheader from 'material-ui/Subheader'
import Paper from 'material-ui/Paper'

import '../appStyle/app.css'
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
            annotationComponent = <EditAnnotation />
        } else if (!this.props.isClean) {
            annotationComponent = <Subheader>Waiting...</Subheader>
        } else {
            annotationComponent = (
                <div>
                    <RaisedButton
                        label="Create Annotation"
                        primary={true}
                        onClick={() => this.props.createAnnotation(this.props.annotationId)}
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

        // Only present navigation icon if we are NOT a dialog
        let iconElementLeft = this.props.allowNavigation ? <IconButton><BackIcon /></IconButton> : <div />

        // Indicate if the annotation information is clean
        let title = `Annotation on ${this.props.annotationId}`
        if (!this.props.isClean) {
            title += " *"
        }

        return (
            <div className='app'>
                <AppBar
                    title={title}
                    iconElementLeft={iconElementLeft}
                    onLeftIconButtonTouchTap={() => this.props.history.push(`/${this.props.indexUuid}`)}
                    iconElementRight={<ErrorDisplay />}
                    />
                <SnackbarDisplay />
                <ApiCallSpinner />
                <Paper className='app--body' zDepth={0}>
                    {annotationComponent}
                </Paper>
            </div>
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
    isClean: PropTypes.bool.isRequired,

    // Connected to redux actions
    createAnnotation: PropTypes.func.isRequired,
    fetchAnnotation: PropTypes.func.isRequired
}

export default SingleAnnotationPage