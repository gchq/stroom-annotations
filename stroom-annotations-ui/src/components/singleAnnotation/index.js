import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import SingleAnnotation from './singleAnnotation'

import { fetchAnnotation } from '../../actions/fetchAnnotation'
import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'
import { createAnnotation } from '../../actions/createAnnotation'

const ConnectedSingleAnnotation = connect(
    (state) => ({
        isClean: state.singleAnnotation.isClean,
        annotation: state.singleAnnotation.annotation
    }),
    {
        createAnnotation,
        fetchAnnotation,
        fetchAnnotationHistory
    }
)(withRouter(SingleAnnotation));

export default ConnectedSingleAnnotation