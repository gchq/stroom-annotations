import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import SingleAnnotationPage from './singleAnnotationPage'

import { fetchAnnotation } from '../../actions/fetchAnnotation'
import { createAnnotation } from '../../actions/createAnnotation'

export default connect(
    (state) => ({
        isClean: state.singleAnnotation.isClean,
        annotation: state.singleAnnotation.annotation,
        indexUuid: state.ui.indexUuid,
        annotationId: state.ui.annotationId,
        allowNavigation: state.ui.allowNavigation
    }),
    {
        createAnnotation,
        fetchAnnotation
    }
)(withRouter(SingleAnnotationPage));
