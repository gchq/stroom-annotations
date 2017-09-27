import React from 'react';
import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import EditAnnotation from '../editAnnotation';
import CreateAnnotation from '../createAnnotation';
import ErrorDisplay from '../errorDisplay';

import PendingUpdatesSpinner from '../pendingUpdatesSpinner'

import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper';

import './App.css'

let App = (props) => {
    let errorComponent = undefined;
    if (props.annotation.errorMsg) {
        errorComponent = <ErrorDisplay />
    }

    let annotationComponent = undefined;

    if (props.annotation.isFetching) {
        annotationComponent = (
            <div>
                <p>Fetching Annotation...</p>
            </div>
        )
    } else if (props.annotation.annotation.id) {
        annotationComponent = <EditAnnotation />
    } else {
        annotationComponent = <CreateAnnotation />
    }

    let iconElementLeft = props.isDialog ? <div /> : undefined;

    let goToManage = () => {
        props.history.push('/');
    }

    let title = `Annotation on ${props.annotation.annotationId}`

    if (!props.annotation.isClean) {
        title += " *"
    }

    return (
        <div className='app'>
            <AppBar
                title={title}
                iconElementLeft={iconElementLeft}
                onLeftIconButtonTouchTap={goToManage}
                iconElementRight={<PendingUpdatesSpinner />}
                />
            <Paper className='app--body'>
            {annotationComponent}
            {errorComponent}
            </Paper>
        </div>
    );
}

export default App = connect(
    (state) => ({
        annotation: state.annotation
    }),
    null
)(withRouter(App));
