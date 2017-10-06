import React, { Component } from 'react';
import PropTypes from 'prop-types'

import EditAnnotation from '../editAnnotation';

import ApiCallSpinner from '../apiCallSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'
import History from '../history'

import IconButton from 'material-ui/IconButton'
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back'
import AppBar from 'material-ui/AppBar'
import RaisedButton from 'material-ui/RaisedButton'
import Subheader from 'material-ui/Subheader'
import Paper from 'material-ui/Paper'

import '../appStyle/app.css'

class SingleAnnotation extends Component {
    componentDidMount() {
        this.props.fetchAnnotation(this.props.annotationId)
        this.props.fetchAnnotationHistory(this.props.annotationId)
    }

    render() {
        let annotationComponent = undefined;

        // Decide on the annotation component
        if (this.props.annotation.id) {
            annotationComponent = <EditAnnotation />
        } else if (!this.props.isClean) {
            annotationComponent = <Subheader>Waiting...</Subheader>
        } else {
            annotationComponent = <RaisedButton
                                      label="Create Annotation"
                                      primary={true}
                                      onClick={() => this.props.createAnnotation(this.props.annotationId)}
                                      />
        }

        // Only present navigation icon if we are NOT a dialog
        let iconElementLeft = this.props.isDialog ? <div /> : <IconButton><BackIcon /></IconButton>;

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
                    onLeftIconButtonTouchTap={() => this.props.history.push('/')}
                    iconElementRight={<ErrorDisplay />}
                    />
                <SnackbarDisplay />
                <ApiCallSpinner />
                <Paper className='app--body' zDepth={0}>
                    {annotationComponent}
                    <History />
                </Paper>
            </div>
        );
    }
}

SingleAnnotation.propTypes = {
    // set from routing
    annotationId: PropTypes.string.isRequired,
    isDialog: PropTypes.bool.isRequired,

    // set by react router
    history: PropTypes.object.isRequired,

    // Conencted to redux state
    annotation: PropTypes.object.isRequired,
    isClean: PropTypes.bool.isRequired,

    // Connected to redux actions
    createAnnotation: PropTypes.func.isRequired,
    fetchAnnotation: PropTypes.func.isRequired,
    fetchAnnotationHistory: PropTypes.func.isRequired
}

export default SingleAnnotation