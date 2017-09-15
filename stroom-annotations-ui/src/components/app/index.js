import React from 'react';
import { connect } from 'react-redux'
import {NavLink} from 'react-router-dom'

import EditAnnotation from '../editAnnotation';
import CreateAnnotation from '../createAnnotation';
import ErrorDisplay from '../errorDisplay';
import CleanIndicator from '../cleanIndicator';

import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper';

import './App.css'
import logo from './logo.svg'

let App = (props) => {
    let errorComponent = undefined;
    if (props.annotation.errorMsg) {
        errorComponent = <ErrorDisplay />
    }

    let annotationComponent = undefined;

    if (props.annotation.isFetching) {
        annotationComponent = (
            <div>
                <p>No Annotation ID Specified</p>
            </div>
        )
    } else if (props.annotation.annotation.id) {
        annotationComponent = <EditAnnotation />
    } else {
        annotationComponent = <CreateAnnotation />
    }

    return (
        <div className='app'>
            <AppBar
                title={<NavLink to='/'><img src={logo} className="app--logo" alt="Stroom logo"/></NavLink>}
                iconElementLeft={<div/>}
                iconElementRight={<CleanIndicator />}
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
)(App);
