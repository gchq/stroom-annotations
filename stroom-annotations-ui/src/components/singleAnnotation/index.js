import React, { Component } from 'react';
import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import EditAnnotation from '../editAnnotation';

import PendingUpdatesSpinner from '../pendingUpdatesSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'
import AnnotationHistory from '../annotationHistory'

import IconButton from 'material-ui/IconButton'
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back'
import AppBar from 'material-ui/AppBar'
import Subheader from 'material-ui/Subheader'
import RaisedButton from 'material-ui/RaisedButton'
import Paper from 'material-ui/Paper'

import { fetchAnnotation } from '../../actions/fetchAnnotation'
import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'
import { fetchStatusValues } from '../../actions/fetchStatusValues'
import { createAnnotation } from '../../actions/createAnnotation'

import '../appStyle/app.css'

class SingleAnnotation extends Component {
    componentDidMount() {
        if (this.props.annotationId) {
            this.props.fetchAnnotation(this.props.annotationId)
            this.props.fetchAnnotationHistory(this.props.annotationId)
            this.props.fetchStatusValues()
        }
    }

    render() {
        let annotationComponent = undefined;

        // Decide on the annotation component
        if (!this.props.annotationId) {
            annotationComponent = (
                <Subheader>No Annotation ID Given</Subheader>
            )
        } else if (this.props.singleAnnotation.isFetching) {
            annotationComponent = (
                <Subheader>Fetching Annotation...</Subheader>
            )
        } else if (this.props.singleAnnotation.annotation.id) {
            annotationComponent = <EditAnnotation />
        } else {
            annotationComponent = <RaisedButton
                                      label="Create Annotation"
                                      primary={true}
                                      onClick={() => this.props.createAnnotation(this.props.annotationId)}
                                      />
        }

        // Only present navigation icon if we are NOT a dialog
        let iconElementLeft = this.props.isDialog ? <div /> : <IconButton><BackIcon /></IconButton>;
        let goToManage = () => {
            this.props.history.push('/');
        }

        // Indicate if the annotation information is clean
        let title = `Annotation on ${this.props.singleAnnotation.annotationId}`
        if (!this.props.singleAnnotation.isClean) {
            title += " *"
        }

        return (
            <div className='app'>
                <AppBar
                    title={title}
                    iconElementLeft={iconElementLeft}
                    onLeftIconButtonTouchTap={goToManage}
                    iconElementRight={<ErrorDisplay />}
                    />
                <SnackbarDisplay />
                <PendingUpdatesSpinner />
                <Paper className='app--body' zDepth={0}>
                    {annotationComponent}
                    <AnnotationHistory />
                </Paper>
            </div>
        );
    }
}

export default SingleAnnotation = connect(
    (state) => ({
        singleAnnotation: state.singleAnnotation
    }),
    {
        createAnnotation,
        fetchAnnotation,
        fetchAnnotationHistory,
        fetchStatusValues
    }
)(withRouter(SingleAnnotation));
