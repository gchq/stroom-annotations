import React, { Component } from 'react';
import { connect } from 'react-redux'
import {
  BrowserRouter as Router,
  Route
} from 'react-router-dom'

import EditAnnotation from './EditAnnotation';
import CreateAnnotation from './CreateAnnotation';

class App extends Component {
  render() {
    let annotationComponent = undefined;
    if (this.props.annotation.id) {
        annotationComponent = <EditAnnotation
                                    updateAnnotation={this.props.updateAnnotation}
                                    removeAnnotation={this.props.removeAnnotation}
                                    annotation={this.props.annotation} />
    } else {
        annotationComponent = (
            <Router>
                <Route path="/:annotationId" component={({match}) => (
                    <CreateAnnotation
                        annotationId={match.params.annotationId} />
                )}/>
            </Router>
        )
    }

    return (
        <div>
            <div>
                <h2>Stroom Annotation Editor</h2>
            </div>
            {annotationComponent}

        </div>
    );
  }
}

export default connect(
    (state) => ({
        annotation: state.annotation
    }),
    null
)(App);
