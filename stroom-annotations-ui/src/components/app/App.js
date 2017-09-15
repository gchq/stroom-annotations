import React from 'react';
import { connect } from 'react-redux'
import {NavLink} from 'react-router-dom'

import EditAnnotation from '../EditAnnotation';
import CreateAnnotation from '../CreateAnnotation';
import Waiting from '../Waiting';
import Error from '../Error';
import CleanIndicator from '../CleanIndicator';

import AppBar from 'material-ui/AppBar'

import './App.css'
import logo from './logo.svg'

let App = (props) => {
    let errorComponent = undefined;
    if (props.annotation.errorMsg) {
        errorComponent = <Error />
    }

    let annotationComponent = undefined;

    if (props.annotation.isFetching) {
        annotationComponent = <Waiting />
    } else if (props.annotation.annotation.id) {
        annotationComponent = <EditAnnotation />
    } else {
        annotationComponent = <CreateAnnotation />
    }

    return (
        <div className='App'>
            <AppBar
                title={<NavLink to='/'><img src={logo} className="App-logo" alt="Stroom logo"/></NavLink>}
                iconElementLeft={<div/>}
                />
            {annotationComponent}
            {errorComponent}
            <CleanIndicator />
        </div>
    );
}

export default App = connect(
    (state) => ({
        annotation: state.annotation
    }),
    null
)(App);
