import React from 'react';
import { connect } from 'react-redux'

import { createAnnotation } from '../actions';

const CreateAnnotation = ({annotationId, createAnnotation}) => (
    <div>
        <button onClick={() => createAnnotation(annotationId)}>Create Annotation</button>
    </div>
)

export default connect(
    null,
    {createAnnotation}
)(CreateAnnotation)
