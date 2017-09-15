import React from 'react';
import { connect } from 'react-redux'

import { createAnnotation } from '../actions/createAnnotation';

import RaisedButton from 'material-ui/RaisedButton';

export const CreateAnnotation = ({annotationId, createAnnotation}) => (
    <div>
        <RaisedButton
            label="Create Annotation"
            primary={true}
            onClick={() => createAnnotation(annotationId)}
            />
    </div>
)

export default connect(
    (state) => ({
        annotationId: state.annotation.annotationId
    }),
    {createAnnotation}
)(CreateAnnotation)
