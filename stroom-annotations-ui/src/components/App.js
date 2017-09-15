import React from 'react';
import { connect } from 'react-redux'

import EditAnnotation from './EditAnnotation';
import CreateAnnotation from './CreateAnnotation';
import Waiting from './Waiting';
import Error from './Error';
import CleanIndicator from './CleanIndicator';

let App = (props) => {
    let errorComponent = undefined;
    if (props.annotation.errorMsg) {
        errorComponent = <Error />
    }

    let annotationComponent = undefined;

    if (props.annotation.isFetching) {
        annotationComponent = <Waiting />
    } else if (props.annotation.id) {
        annotationComponent = <EditAnnotation />
    } else {
        annotationComponent = <CreateAnnotation />
    }

    return (
        <div>
            <div>
                <h2>Stroom Annotation Editor</h2>
            </div>
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
